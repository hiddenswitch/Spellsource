package net.demilich.metastone;

import com.hiddenswitch.minionate.Client;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.gui.cards.CardProxy;
import net.demilich.metastone.gui.autoupdate.AutoUpdateMediator;
import net.demilich.metastone.gui.deckbuilder.DeckFormatProxy;
import net.demilich.metastone.gui.deckbuilder.DeckProxy;
import net.demilich.metastone.gui.dialog.DialogMediator;
import net.demilich.metastone.gui.main.ApplicationMediator;
import net.demilich.metastone.gui.playmode.animation.AnimationProxy;
import net.demilich.metastone.gui.sandboxmode.SandboxProxy;
import net.demilich.metastone.gui.trainingmode.TrainingProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationStartupCommand extends SimpleCommand<GameNotification> {
	Logger logger = LoggerFactory.getLogger(ApplicationStartupCommand.class);

	@Override
	public void execute(INotification<GameNotification> notification) {
		getFacade().registerMediator(new DialogMediator());

		getFacade().registerProxy(new CardProxy());
		getFacade().registerProxy(new DeckProxy());
		getFacade().registerProxy(new DeckFormatProxy());
		getFacade().registerProxy(new TrainingProxy());
		getFacade().registerProxy(new SandboxProxy());
		getFacade().registerProxy(new AnimationProxy());

		getFacade().registerMediator(new ApplicationMediator());
		getFacade().registerMediator(new AutoUpdateMediator());

		// Load the preferences and set the network API if it exists
		Client.getInstance().loadAccount();
	}
}
