import psycopg2

from contextlib import closing
from psycopg2.extras import NamedTupleCursor

if __name__ == "__main__":
    # with closing(psycopg2.connect(dbname="d3j83j1dltknln",
    #                               user="fevawmuxwpzjxy",
    #                               password="2e1c1c97fa9965502549d607e97baa6fa99bfd5caf85dd9e851aed6a04d0ec79",
    #                               host="ec2-54-75-246-118.eu-west-1.compute.amazonaws.com")) as conn_new:

    with closing(psycopg2.connect(dbname="mathhelper_2021_09", user="postgres", password="@p0stgrEs@", host="mathhelper.space")) as conn_new:
        with conn_new.cursor(cursor_factory=NamedTupleCursor) as cursor_new:
            cursor_new.execute("SELECT * FROM public.user_entity limit 100")
            print(cursor_new.fetchall())
