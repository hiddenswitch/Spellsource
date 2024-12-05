import itertools
import os
import pickle
import random
import re
import tempfile
from typing import List

import numpy as np
from keras.callbacks import ModelCheckpoint
from keras.layers import Dense, Dropout, Embedding, LSTM, TimeDistributed
from keras.models import load_model, save_model, Sequential, Model
from keras.optimizers import Adam

from spellsource.context import Context
from spellsource.ext.cards import iter_cards
from spellsource.ext.loggercallback import LoggerCallback
from spellsource.ext.workspace import Workspace


class CharRNNWorkspace(Workspace):
    VALID_CARD_SETS = frozenset([
        'BASIC',
        'CLASSIC',
        'REWARD',
        'PROMO',
        'NAXXRAMAS',
        'GOBLINS_VS_GNOMES',
        'BLACKROCK_MOUNTAIN',
        'THE_GRAND_TOURNAMENT',
        'LEAGUE_OF_EXPLORERS',
        'THE_OLD_GODS',
        'ONE_NIGHT_IN_KARAZHAN',
        'MEAN_STREETS_OF_GADGETZAN',
        'PROCEDURAL_PREVIEW',
        'JOURNEY_TO_UNGORO',
        'KNIGHTS_OF_THE_FROZEN_THRONE',
        'KOBOLDS_AND_CATACOMBS',
        'WITCHWOOD',
        'HALL_OF_FAME',
        'CUSTOM',
        'BATTLE_FOR_ASHENVALE',
        'SANDS_OF_TIME'
    ])

    CUSTOM_CARDS_PATH = 'cards/src/main/resources/cards/custom'
    HEARTHSTONE_CARDS_PATH = 'internalcontent/src/main/resources/cards/'
    DESCRIPTION_CHARS = ' .,;/+-abcdefghijklmnopqrstuvwxyz'
    ATTACK_COUNT_CHAR = 'A'
    HEALTH_COUNT_CHAR = 'H'
    MANA_COST_COUNT_CHAR = 'M'
    NUMBER_COUNT_CHAR = '#'
    START_SEQ_CHAR = ']'
    END_SEQ_CHAR = '}'
    ZERO_CHAR = '0'
    # START_NAME_CHAR = '['
    KEYWORD_CHARS = {
        r'deathrattle[.:\s]*': 'D',
        r'battlecry[.:\s]*': 'B',
        r'\btaunt\.?\s?': 'T',
        r'\bdivine shield\.?\s?': 'V',
        r'\bstealth\.?\s?': '∫',
        r'(freeze)|(frozen)': 'F',
        r'windfury\.?\s?': 'W',
        r'charge\.?\s?': 'C',
        r'immune\.?\s?': 'I',
        r'secret[.:\s]*': 'R',
        r'combo[.:\s]*': '1',
        r'overload[.:\s]*': 'O',
        r'choose\s?one[.:\s]*': '2',
        # r"(cannot)|(can't)\s?attack\.?\s?": 'K',
        r'\brush\.?': 'U',
        r'\bquest[.:]*': 'Q',
        r'\bdormant\.?': 'G',
        r'\bpoison((ed)|(ous\.?))?': 'P',
        r'lifesteal\.?\s': 'L',
        r'\bcost[s.:\s]*': '$',
        r'start\sof\sgame[.:\s]*': 'Y',
        # r'all': 'å',
        r'\brandom': '®',
        r'\btarget': '×',
        r'\bsilence': 'X',
        r'\bsummon': 'S',
        r'\bdiscover': '∂',
        r'\bminions?': 'µ',
        r'\bhero(e?s)?': '♔',
        r'characters?': 'K',
        r'weapons?': '⚔︎',
        r'\bspells?': '∭',
        # r'(yours?)|(friend(ly)?)': '☟',
        # r'(enemy|theirs?|opposing)': '☝︎',
        # r'gives?': '→',
        r'gains?': '↑',
        r'loses?': '↓',
        # r'whenever': '◎',
        # r'(every)|(each)': '◼',
        # r'turn': '▽',
        r'damage': '⚡',
        r'health': '✝',
        r'\band\b': '&',
        r'\bor\b': '|',
        r'\bzero\b': ZERO_CHAR
    }

    VALID_CHARS = DESCRIPTION_CHARS + ATTACK_COUNT_CHAR + HEALTH_COUNT_CHAR + MANA_COST_COUNT_CHAR + NUMBER_COUNT_CHAR \
                  + ''.join(KEYWORD_CHARS.values()) + START_SEQ_CHAR + END_SEQ_CHAR  # + START_NAME_CHAR
    PADDING_CHAR = '_'

    def __init__(self, batch_size=32, max_epochs=None):
        self.batch_size = batch_size
        self._create_dictionary()
        # Load text, convert it into a "sequence"
        hearthcards = pickle.load(
            open(Context.find_resource_path(filename='hearthcards.pkl'), 'rb'))  # type: List[dict]
        spellsource = iter_cards(start_path=Context.find_resource_path('cards'))

        training_names = frozenset(card['cardname'].lower() for card in hearthcards)
        training = [CharRNNWorkspace._format_hearthcard(card) for card in hearthcards]
        # Remove cards that exist both in the validation set and training sets by comparing the names
        validation = [CharRNNWorkspace._format_card(**card) for card in spellsource if
                      card['name'].lower() not in training_names and card['set'] in CharRNNWorkspace.VALID_CARD_SETS]
        assert len(training) > 0
        assert len(validation) > 0
        # validation = [card for card in spellsource if card['name'].strip().lower() not in training_names]
        self.seq_len = max(len(s) for s in itertools.chain(training, validation))
        assert self.seq_len > 0
        self.training = self._prepare_data_set(training)
        self.validation = self._prepare_data_set(validation)
        self.epoch = 0
        self.max_epochs = max_epochs or self.seq_len * 2
        assert self.max_epochs > 0
        self.model = self._build_model(batch_size=self.batch_size, seq_len=self.seq_len, vocab_size=self.vocab_size)
        self.inference_model = CharRNNWorkspace._build_inference_model(self.model)

    def _prepare_data_set(self, data_set: [str]) -> np.ndarray:
        """
        Prepares a data set by encoding it
        :param data_set:
        :return:
        """
        # right pad
        data_set = [CharRNNWorkspace.PADDING_CHAR * (self.seq_len - len(s)) + s for s in data_set]
        data_set = ''.join(data_set)
        data_set = self._encode_text(data_set)
        return data_set

    def _create_dictionary(self):
        """
        create char2id, id2char and vocab_size
        from printable ascii characters.
        """
        chars = CharRNNWorkspace.VALID_CHARS
        self.char2id = dict((ch, i + 1) for i, ch in enumerate(chars))
        self.char2id.update({"": 0})
        self.id2char = dict((self.char2id[ch], ch) for ch in self.char2id)
        self.vocab_size = len(self.char2id)

    def _build_model(self, batch_size: int, seq_len: int, vocab_size, embedding_size=32,
                     rnn_size=128, num_layers=2, drop_rate=0.0,
                     learning_rate=0.001, clip_norm=5.0) -> Sequential:
        """
        build character embeddings LSTM text generation model.
        """
        print("building model: batch_size=%s, seq_len=%s, vocab_size=%s, "
              "embedding_size=%s, rnn_size=%s, num_layers=%s, drop_rate=%s, "
              "learning_rate=%s, clip_norm=%s." % (
                  batch_size, seq_len, vocab_size, embedding_size,
                  rnn_size, num_layers, drop_rate,
                  learning_rate, clip_norm))
        model = Sequential()
        # input shape: (batch_size, seq_len)
        model.add(Embedding(vocab_size, embedding_size, mask_zero=True,
                            batch_input_shape=(batch_size, seq_len)))
        model.add(Dropout(drop_rate))
        # shape: (batch_size, seq_len, embedding_size)
        for _ in range(num_layers):
            model.add(LSTM(rnn_size, return_sequences=True, stateful=True))
            model.add(Dropout(drop_rate))
        # shape: (batch_size, seq_len, rnn_size)
        model.add(TimeDistributed(Dense(vocab_size, activation="softmax")))
        # output shape: (batch_size, seq_len, vocab_size)
        optimizer = Adam(learning_rate, clipnorm=clip_norm)
        model.compile(loss="categorical_crossentropy", optimizer=optimizer)
        return model

    @staticmethod
    def _build_inference_model(model: Model, batch_size=1, seq_len=1) -> Model:
        """
        build inference model from model config
        input shape modified to (1, 1)
        """
        print("building inference model.")
        config = model.get_config()
        # edit batch_size and seq_len
        config[0]["config"]["batch_input_shape"] = (batch_size, seq_len)
        inference_model = Sequential.from_config(config)
        inference_model.trainable = False
        return inference_model

    @staticmethod
    def _format_hearthcard(in_card: dict) -> str:
        description = in_card['cardtext']
        name = in_card['cardname']
        baseAttack = in_card['attack']
        baseHp = in_card['health']
        baseManaCost = in_card['mana']

        return CharRNNWorkspace._format_card(baseAttack, baseHp, baseManaCost, description, name)

    @staticmethod
    def _format_card(baseAttack=0, baseHp=0, baseManaCost=0, description='', name='', **kwargs):
        def _replace_numbers(in_str: str, repl_char: str, zero='') -> str:
            mutable = in_str[:]
            for match in reversed(list(re.finditer(pattern=r'(\d+)', string=in_str))):
                i, j = match.span(0)
                number = max(min(int(match.group(0)), 12), 0)
                mutable = mutable[0:i] + (zero if number == 0 else repl_char * number) + mutable[j:len(mutable)]
            return mutable

        def _replace_keywords(in_str: str) -> str:
            for keyword, repl in CharRNNWorkspace.KEYWORD_CHARS.items():
                in_str = re.sub(pattern=keyword, string=in_str, repl=repl, flags=re.IGNORECASE)
            return in_str

        def _without_tags(in_str: str) -> str:
            return re.sub(pattern=r'(\[/?[bi]\])|(&\w+;)', string=in_str, repl='')

        def _without_llegals(in_str: str) -> str:
            return re.sub(pattern=r'[^%s]' % re.escape(CharRNNWorkspace.VALID_CHARS), string=in_str, repl='')

        def _without_newlines(in_str: str) -> str:
            return re.sub(pattern=r'(\r\n)|(\n)', string=in_str, repl='. ')

        out_description = \
            _without_llegals(
                _replace_keywords(
                    _replace_numbers(
                        _without_newlines(
                            _without_tags(
                                description.lower())), repl_char=CharRNNWorkspace.NUMBER_COUNT_CHAR)))
        # Omit name for now
        # out_name = _without_llegals(_without_tags(_without_newlines(name.lower())))
        out_attack = CharRNNWorkspace.ATTACK_COUNT_CHAR * max(min(int(baseAttack), 12), 0)
        out_health = CharRNNWorkspace.HEALTH_COUNT_CHAR * max(min(int(baseHp), 12), 0)
        out_base_mana_cost = CharRNNWorkspace.MANA_COST_COUNT_CHAR * max(min(int(baseManaCost), 12), 0)
        encoded = out_base_mana_cost + out_attack + out_health + \
                  CharRNNWorkspace.START_SEQ_CHAR + out_description + CharRNNWorkspace.END_SEQ_CHAR
        return encoded

    def _encode_text(self, text: str) -> np.ndarray:
        """
        encode text to array of integers with CHAR2ID
        """
        return np.fromiter((self.char2id.get(ch, 0) for ch in text), int)

    def _decode_text(self, int_array: np.ndarray) -> str:
        """
        decode array of integers to text with ID2CHAR
        """
        return "".join((self.id2char[ch] for ch in int_array))

    def _one_hot_encode(self, indices, num_classes: int) -> np.ndarray:
        """
        one-hot encoding
        """
        return np.eye(num_classes)[indices]

    def generate_text(self, seed: np.ndarray, top_n=10):
        """
        generates text of specified length from trained model
        with given seed character sequence.
        :param model:
        :param seed:
        """
        model = self.get_inference_model()
        # logger.info("generating %s characters from top %s choices.", length, top_n)
        # logger.info('generating with seed: "%s".', seed)
        generated = self._decode_text(seed)
        encoded = seed[:]
        model.reset_states()

        for idx in encoded[:-1]:
            x = np.array([[idx]])
            # input shape: (1, 1)
            # set internal states
            model.predict(x)

        next_index = encoded[-1]
        for i in range(self.seq_len):
            x = np.array([[next_index]])
            # input shape: (1, 1)
            probs = model.predict(x)
            # output shape: (1, 1, vocab_size)
            next_index = self._sample_from_probs(probs.squeeze(), top_n)
            # append to sequence
            generated += self.id2char[next_index]

        # logger.info("generated text: \n%s\n", generated)
        return generated

    def _batch_generator(self, sequence: np.ndarray, batch_size=64, seq_len=64, one_hot_features=False,
                         one_hot_labels=False):
        """
        batch generator for sequence
        ensures that batches generated are continuous along axis 1
        so that hidden states can be kept across batches and epochs
        """
        # calculate effective length of text to use
        num_batches = (len(sequence) - 1) // (batch_size * seq_len)
        if num_batches == 0:
            raise ValueError("No batches created. Use smaller batch size or sequence length.")
        # logger.info("number of batches: %s.", num_batches)
        rounded_len = num_batches * batch_size * seq_len
        # logger.info("effective text length: %s.", rounded_len)

        x = np.reshape(sequence[: rounded_len], [batch_size, num_batches * seq_len])
        if one_hot_features:
            x = self._one_hot_encode(x, self.vocab_size)
        # logger.info("x shape: %s.", x.shape)

        y = np.reshape(sequence[1: rounded_len + 1], [batch_size, num_batches * seq_len])
        if one_hot_labels:
            y = self._one_hot_encode(y, self.vocab_size)
        # logger.info("y shape: %s.", y.shape)

        while True:
            # roll so that no need to reset rnn states over epochs
            x_epoch = np.split(np.roll(x, -self.epoch, axis=0), num_batches, axis=1)
            y_epoch = np.split(np.roll(y, -self.epoch, axis=0), num_batches, axis=1)
            for batch in range(num_batches):
                yield x_epoch[batch], y_epoch[batch]
            self.epoch += 1

    def train(self):
        epochs = self.max_epochs - self.epoch

        # logger.info("model saved: %s.", args.checkpoint_path)
        # callbacks
        checkpoint_path = tempfile.mktemp()
        callbacks = [
            ModelCheckpoint(checkpoint_path, verbose=1, save_best_only=False),
            LoggerCallback(self)
        ]

        save_model(self.model, checkpoint_path)

        # training start
        num_training_batches = (len(self.training) - 1) // (self.batch_size * self.seq_len)
        num_validation_batches = (len(self.validation) - 1) // (self.batch_size * self.seq_len)
        self.model.reset_states()
        try:
            self.model.fit_generator(
                initial_epoch=self.epoch,
                generator=self._batch_generator(self.training, self.batch_size, self.seq_len, one_hot_labels=True),
                steps_per_epoch=num_training_batches,
                validation_data=self._batch_generator(self.validation, self.batch_size, self.seq_len,
                                                      one_hot_labels=True),
                validation_steps=num_validation_batches,
                epochs=epochs,
                callbacks=callbacks)
        except KeyboardInterrupt as interrupted:
            # Restore the last saved checkpoint here
            self.model = load_model(checkpoint_path)
            if self.epoch == 0:
                print('This model has not finished a complete epoch, weights will be saved without configuration.')
            raise interrupted

    def get_inference_model(self) -> Model:
        return self.inference_model

    @staticmethod
    def _make_dirs(path, empty=False):
        """
        create dir in path and clear dir if required
        """
        dir_path = os.path.dirname(path)
        os.makedirs(dir_path, exist_ok=True)

        if empty:
            files = [os.path.join(dir_path, item) for item in os.listdir(dir_path)]
            for item in files:
                if os.path.isfile(item):
                    os.remove(item)

        return dir_path

    @staticmethod
    def _sample_from_probs(probs, top_n=10):
        """
        truncated weighted random choice.
        """
        # need 64 floating point precision
        probs = np.array(probs, dtype=np.float64)
        # set probabilities after top_n to 0
        probs[np.argsort(probs)[:-top_n]] = 0
        # renormalise probabilities
        probs /= np.sum(probs)
        sampled_index = np.random.choice(len(probs), p=probs)
        return sampled_index

    def generate_seed(self) -> np.ndarray:
        """
        create a valid, balanced card prefix
        :param seq_lens:
        :return:
        """
        mana_cost = random.randint(0, 10)
        attack = random.randint(max(0, mana_cost - mana_cost // 2), min(10, mana_cost + mana_cost // 2))
        health = random.randint(max(0, mana_cost - mana_cost // 2), min(10, mana_cost + mana_cost // 2))
        is_spell = random.random() < 0.25
        if is_spell:
            attack = 0
            health = 0
        return self._encode_text(
            mana_cost * CharRNNWorkspace.MANA_COST_COUNT_CHAR + attack * CharRNNWorkspace.ATTACK_COUNT_CHAR + health \
            * CharRNNWorkspace.HEALTH_COUNT_CHAR + CharRNNWorkspace.START_SEQ_CHAR)

    @staticmethod
    def _make_keras_picklable():
        def __getstate__(self):
            model_str = ""
            with tempfile.NamedTemporaryFile(suffix='.hdf5', delete=True) as fd:
                save_model(self, fd.name, overwrite=True)
                model_str = fd.read()
            d = {'model_str': model_str}
            return d

        def __setstate__(self, state):
            with tempfile.NamedTemporaryFile(suffix='.hdf5', delete=True) as fd:
                fd.write(state['model_str'])
                fd.flush()
                model = load_model(fd.name)
            self.__dict__ = model.__dict__

        cls = Model
        cls.__getstate__ = __getstate__
        cls.__setstate__ = __setstate__


CharRNNWorkspace._make_keras_picklable()

if __name__ == '__main__':
    path = 'charrnnn_checkpoint.bin'

    if os.path.exists(path):
        try:
            workspace = pickle.load(open(path, 'rb'))  # type: CharRNNWorkspace
            print('Loaded progress (%d epochs) from path %s' % (workspace.epoch, path))
        except Exception as ex:
            print('Failed to load workspace, creating a new one')
            print(ex)
            workspace = CharRNNWorkspace(batch_size=32, max_epochs=1000)
    else:
        workspace = CharRNNWorkspace(batch_size=32, max_epochs=1000)
    try:
        workspace.train()
    except KeyboardInterrupt as ex:
        pickle.dump(workspace, open(path, 'wb'))
        print()
        print('Saved progress to path %s' % path)
