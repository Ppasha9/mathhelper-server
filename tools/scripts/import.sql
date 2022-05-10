-- index func
create or replace function public.make_tsvector_by_keywords(p_keywords text) returns tsvector
    immutable
    language plpgsql
as
'
BEGIN
    RETURN (select to_tsvector(''english'', p_keywords) || to_tsvector(''russian'', p_keywords));
END
';


-- TASKSETS --
drop function if exists public.find_tasksets_by_keywords(varchar, varchar, varchar, varchar, int, int);
create or replace function public.find_tasksets_by_keywords(p_keywords varchar, p_namespace_code varchar, p_author_user_code varchar, p_subject_type varchar, p_rows_limit int, p_offset int)
    returns setof varchar
    stable
    language plpgsql
as
'
DECLARE
    v_query tsquery = to_tsquery(''english'', p_keywords) || to_tsquery(''russian'', p_keywords);
BEGIN
    return query
        SELECT public.taskset.code as code
        FROM public.taskset
        WHERE
            (make_tsvector_by_keywords(public.taskset.keywords) @@ v_query) AND
            (p_namespace_code = '''' OR public.taskset.namespace_code = p_namespace_code) AND
            (p_author_user_code = '''' OR public.taskset.author_user_code = p_author_user_code) AND
            (p_subject_type = '''' OR public.taskset.subject_type = p_subject_type)
        ORDER BY ts_rank(make_tsvector_by_keywords(public.taskset.keywords), v_query)
        limit p_rows_limit offset p_offset;
END
';

CREATE OR REPLACE FUNCTION function_add_taskset_word() RETURNS TRIGGER AS
'
BEGIN
    INSERT INTO public.taskset_word(word)
    select tt.word
    from (select (unnest(to_tsvector(''simple'', new.keywords))).lexeme as word) tt
    where not exists(select 1 from public.taskset_word words_t where words_t.word = tt.word);

    RETURN new;
END;
' language plpgsql;

-- TASKS --
drop function if exists public.find_tasks_by_keywords(varchar, varchar, varchar, varchar, int, int);
create or replace function public.find_tasks_by_keywords(p_keywords varchar, p_namespace_code varchar, p_author_user_code varchar, p_subject_type varchar, p_rows_limit int, p_offset int)
    returns setof varchar
    stable
    language plpgsql
as
'
DECLARE
    v_query tsquery = to_tsquery(''english'', p_keywords) || to_tsquery(''russian'', p_keywords);
BEGIN
    return query
        SELECT public.task.code
        FROM public.task
        WHERE
            (make_tsvector_by_keywords(keywords) @@ v_query) AND
            (p_namespace_code = '''' OR public.task.namespace_code = p_namespace_code) AND
            (p_author_user_code = '''' OR public.task.author_user_code = p_author_user_code) AND
            (p_subject_type = '''' OR public.task.subject_type = p_subject_type)
        ORDER BY ts_rank(make_tsvector_by_keywords(keywords), v_query)
        limit p_rows_limit offset p_offset;
END
';

CREATE OR REPLACE FUNCTION function_add_task_word() RETURNS TRIGGER AS
'
BEGIN
    INSERT INTO public.task_word(word)
    select tt.word
    from (select (unnest(to_tsvector(''simple'', new.keywords))).lexeme as word) tt
    where not exists(select 1 from public.task_word words_t where words_t.word = tt.word);

    RETURN new;
END;
' language plpgsql;

-- RULE PACKS --
drop function if exists public.find_rule_packs_by_keywords(varchar, varchar, varchar, int, int);
create or replace function public.find_rule_packs_by_keywords(p_keywords varchar, p_namespace_code varchar, p_subject_type varchar, p_rows_limit int, p_offset int)
    returns setof varchar
    stable
    language plpgsql
as
'
DECLARE
    v_query tsquery = to_tsquery(''english'', p_keywords) || to_tsquery(''russian'', p_keywords);
BEGIN
    return query
        SELECT public.rule_pack.code
        FROM public.rule_pack
        WHERE
            (make_tsvector_by_keywords(keywords) @@ v_query) AND
            (p_namespace_code = '''' OR public.rule_pack.namespace_code = p_namespace_code) AND
            (p_subject_type = '''' OR public.rule_pack.subject_type = p_subject_type)
        ORDER BY ts_rank(make_tsvector_by_keywords(keywords), v_query)
        limit p_rows_limit offset p_offset;
END
';

CREATE OR REPLACE FUNCTION function_add_rule_pack_word() RETURNS TRIGGER AS
'
BEGIN
    INSERT INTO public.rule_pack_word(word)
    select tt.word
    from (select (unnest(to_tsvector(''simple'', new.keywords))).lexeme as word) tt
    where not exists(select 1 from public.rule_pack_word words_t where words_t.word = tt.word);

    RETURN new;
END;
' language plpgsql;
