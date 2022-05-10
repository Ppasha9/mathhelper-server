if __name__ == "__main__":
    import psycopg2

    from contextlib import closing
    from psycopg2.extras import NamedTupleCursor, Json

    old_ratings = []

    with closing(psycopg2.connect(dbname="postgres", user="postgres", password="@p0stgrEs@", host="185.224.139.44")) as conn_old:
        with conn_old.cursor(cursor_factory=NamedTupleCursor) as cursor_old:
            cursor_old.execute("SELECT wl.winlog_id, wl.user_code, wl.game_code, wl.game_version, gn.name, wl.level_code, wl.comment, wl.difficulty, wl.client_action_ts, wl.server_action_ts, wl.context, wl.curr_steps_number FROM public.win_log wl LEFT JOIN public.games gn ON wl.game_code = gn.code and wl.game_version = gn.version")
            old_ratings = cursor_old.fetchall()

    # with closing(psycopg2.connect(dbname="d3j83j1dltknln",
    #                               user="fevawmuxwpzjxy",
    #                               password="2e1c1c97fa9965502549d607e97baa6fa99bfd5caf85dd9e851aed6a04d0ec79",
    #                               host="ec2-54-75-246-118.eu-west-1.compute.amazonaws.com")) as conn:
    #     with conn.cursor(cursor_factory=NamedTupleCursor) as cursor:
    #         cursor.execute("truncate public.old_rating")
    #         conn.commit()

    # with closing(psycopg2.connect(dbname="d3j83j1dltknln",
    #                               user="fevawmuxwpzjxy",
    #                               password="2e1c1c97fa9965502549d607e97baa6fa99bfd5caf85dd9e851aed6a04d0ec79",
    #                               host="ec2-54-75-246-118.eu-west-1.compute.amazonaws.com")) as conn:

    with closing(psycopg2.connect(dbname="mathhelper_2021_09", user="postgres", password="@p0stgrEs@", host="mathhelper.space")) as conn:
        with conn.cursor(cursor_factory=NamedTupleCursor) as cursor:
            for rating in old_ratings:
                if int(rating.winlog_id) <= 3695:
                    continue

                query = f"INSERT INTO public.old_rating (id, user_code, app_code, taskset_code, taskset_version, taskset_name, task_code, comment, difficulty, client_action_ts, server_action_ts, context, curr_steps_number) VALUES ({rating.winlog_id}, '{rating.user_code}', 'matify', '{rating.game_code}', {rating.game_version}, '{rating.name}', '{rating.level_code}', '{rating.comment}', {0.0 if rating.difficulty is None else rating.difficulty}, '{str(rating.client_action_ts)}', '{str(rating.server_action_ts)}', {Json(rating.context)}, {rating.curr_steps_number})"
                cursor.execute(query)
            conn.commit()
