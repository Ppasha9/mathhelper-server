import psycopg2

from contextlib import closing
from psycopg2.extras import NamedTupleCursor

if __name__ == "__main__":
    """
    Structure of `users` table in old database is:
    | code: String | user_type_code: String | login: String | email: String | additional: String | name: String | full_name: String | locale: String | password: String | external_code: String | is_oauth: Boolean

    User type codes in old database
    const val ADMIN_USER_TYPE_CODE = "admin"
    const val CASUAL_USER_TYPE_CODE = "casual"
    
    User type codes in new database:
    const val ADMIN_USER_TYPE_CODE = "admin"
    const val DEFAULT_USER_TYPE_CODE = "default"

    Structure of `user_entity` table in new database is:
    | code: String | user_type_code: String | login: String | email: String | additional: String | name: String | full_name: String | locale: String | password: String | external_code: String | is_oauth: Boolean | other_data: {}
    """

    old_users = []

    with closing(psycopg2.connect(dbname="postgres", user="postgres", password="@p0stgrEs@", host="185.224.139.44")) as conn_old:
        with conn_old.cursor(cursor_factory=NamedTupleCursor) as cursor_old:
            cursor_old.execute("SELECT * FROM public.users")
            old_users = cursor_old.fetchall()

    # with closing(psycopg2.connect(dbname="d3j83j1dltknln",
    #                               user="fevawmuxwpzjxy",
    #                               password="2e1c1c97fa9965502549d607e97baa6fa99bfd5caf85dd9e851aed6a04d0ec79",
    #                               host="ec2-54-75-246-118.eu-west-1.compute.amazonaws.com")) as conn_new:

    with closing(psycopg2.connect(dbname="mathhelper_2021_09", user="postgres", password="@p0stgrEs@", host="mathhelper.space")) as conn_new:
        with conn_new.cursor() as cursor_new:
            for user in old_users:
                has_login = False
                has_email = False

                query = "INSERT INTO public.user_entity (code, user_type_code, additional, name, full_name, locale, password, external_code, is_oauth"
                if user.login is not None:
                    has_login = True
                    query += ", login"
                if user.email is not None:
                    has_email = True
                    query += ", email"
                query += ") VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'"
                if has_login:
                    query += ", '%s'"
                if has_email:
                    query += ", '%s'"
                query += ")"
                args_tuple = (
                    str(user.code),
                    "default" if user.user_type_code == "casual" else str(user.user_type_code),
                    "" if user.additional is None else str(user.additional).replace("\'", "\'\'"),
                    "" if user.name is None else str(user.name).replace("\'", "\'\'"),
                    "" if user.full_name is None else str(user.full_name).replace("\'", "\'\'"),
                    "" if user.locale is None else str(user.locale),
                    "" if user.password is None else str(user.password),
                    "" if user.external_code is None else str(user.external_code),
                    "False" if user.is_oauth is None else str(user.is_oauth)
                )
                if has_login:
                    args_tuple += (str(user.login).replace("\'", "\'\'"),)
                if has_email:
                    args_tuple += (str(user.email).replace("\'", "\'\'"),)

                query = query % args_tuple

                cursor_new.execute(query)

        conn_new.commit()
