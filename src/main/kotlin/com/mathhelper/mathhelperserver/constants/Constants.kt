package com.mathhelper.mathhelperserver.constants

import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.validation.constraints.NotNull

object Constants {
    const val MIN_LENGTH = 3
    const val ALL_EXCEPT_AT_SYMBOL = "([^@])*"
    const val STRING_LENGTH_SHORT = 32
    const val STRING_LENGTH_MIDDLE = 64
    const val STRING_LENGTH_LONG = 256

    const val LOGIN_OR_EMAIL_MIN_LENGTH = 3
    const val PASSWORD_MIN_LENGTH = 6
    const val EMAIL_MAX_LENGTH = 60
    const val NAME_MIN_LENGTH = 3
    const val NAME_MAX_LENGTH = 50
    const val FULL_NAME_MAX_LENGTH = 150

    const val LOCALE_LENGTH = 3

    // Exceptions
    const val EXCEPTION_FAIL_STRING = "Fail."
    const val EXCEPTION_BAD_REQUEST_STRING = "$EXCEPTION_FAIL_STRING Bad Request."
    const val EXCEPTION_INVALID_BODY_STRING = "$EXCEPTION_FAIL_STRING Invalid request body."

    // Languages
    const val ENGLISH_LOCALE_NAME = "eng"
    const val ENGLISH_SYMBOLS = "AfBbCcDfEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
    const val RUSSIAN_LOCALE_NAME = "rus"
    const val RUSSIAN_SYMBOLS = "АаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя"
    const val NUMERIC_SYMBOLS = "0123456789"
    const val ALLOWED_SYMBOLS = ENGLISH_SYMBOLS + RUSSIAN_SYMBOLS + NUMERIC_SYMBOLS
    const val ENGLISH_KEYBOARDS_SYMBOLS = "qwertyuiop[]asdfghjkl;'zxcvbnm,."
    const val RUSSIAN_KEYBOARDS_SYMBOLS = "йцукенгшщзхъфывапролджэячсмитьбю"

    const val CYRILLIC_TO_LATIN = "Cyrillic-Latin"

    // User types codes
    const val DEFAULT_USER_TYPE_CODE = "default"
    const val ADMIN_USER_TYPE_CODE = "admin"

    // Authority
    const val ANY_AUTHORIZED_AUTHORITY = "hasAuthority('${DEFAULT_USER_TYPE_CODE}') or hasAnyAuthority('${ADMIN_USER_TYPE_CODE}')"

    // JWT properties
    const val JWT_SECRET_KEY = "__s@per@secrit_key__"
    const val JWT_EXPIRATION_TIME_MS = 31622400000000

    val MAX_TIME: OffsetDateTime = OffsetDateTime.of(294270, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    // PostgreSQL
    const val POSTGRES_SCHEME = "public"

    // Rating
    const val ACTIVE_USER_COND = 2

    // Grant types
    const val USER_GRANT_TYPE_READ_WRITE = "READ_WRITE"
    const val USER_GRANT_TYPE_READ_NO_WRITE = "READ_NO_WRITE"
    const val USER_GRANT_TYPE_NO_READ_WRITE = "NO_READ_WRITE"

    const val NAMESPACE_GRANT_TYPE_PUBLIC_READ_WRITE = "PUBLIC_READ_WRITE"
    const val NAMESPACE_GRANT_TYPE_PUBLIC_READ_PRIVATE_WRITE = "PUBLIC_READ_PRIVATE_WRITE"
    const val NAMESPACE_GRANT_TYPE_PRIVATE_READ_WRITE = "PRIVATE_READ_WRITE"

    const val KEYWORDS_SEARCHING_LIMIT_MAX_SIZE = 2000
    const val KEYWORDS_SEARCHING_LIMIT_MIN_SIZE = 10

    // Apps codes
    const val APP_CODE_MATIFY_ANDROID = "MATIFY_ANDROID"
    const val APP_CODE_SOLVE_MATH_WEB = "SOLVE_MATH_WEB"
}