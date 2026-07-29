CREATE OR REPLACE FUNCTION spellsource.clustered_games_update_game_and_users(
    p_user_id_winner text,
    p_user_id_loser text,
    p_game_id bigint,
    p_trace json
) RETURNS boolean
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE spellsource.game_users
    SET victory_status = 'WON'::spellsource.game_user_victory_enum
    WHERE user_id = p_user_id_winner
      AND game_id = p_game_id;

    UPDATE spellsource.game_users
    SET victory_status = 'LOST'::spellsource.game_user_victory_enum
    WHERE user_id = p_user_id_loser
      AND game_id = p_game_id;

    UPDATE spellsource.games
    SET status = 'FINISHED'::spellsource.game_state_enum,
        trace = p_trace
    WHERE id = p_game_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'game % was not found while recording completion', p_game_id;
    END IF;

    RETURN true;
END;
$$;
