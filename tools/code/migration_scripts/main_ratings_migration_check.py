if __name__ == "__main__":
    import psycopg2

    from contextlib import closing
    from psycopg2.extras import NamedTupleCursor, Json

    # with closing(psycopg2.connect(dbname="d3j83j1dltknln",
    #                               user="fevawmuxwpzjxy",
    #                               password="2e1c1c97fa9965502549d607e97baa6fa99bfd5caf85dd9e851aed6a04d0ec79",
    #                               host="ec2-54-75-246-118.eu-west-1.compute.amazonaws.com")) as conn:

    with closing(psycopg2.connect(dbname="mathhelper_2021_09", user="postgres", password="@p0stgrEs@", host="mathhelper.space")) as conn:
        with conn.cursor(cursor_factory=NamedTupleCursor) as cursor:
            cursor.execute("SELECT * FROM public.old_rating")
            print(len(cursor.fetchall()))

    with closing(psycopg2.connect(dbname="postgres", user="postgres", password="@p0stgrEs@", host="185.224.139.44")) as conn_old:
        with conn_old.cursor(cursor_factory=NamedTupleCursor) as cursor_old:
            cursor_old.execute("SELECT * FROM public.win_log")
            print(len(cursor_old.fetchall()))
