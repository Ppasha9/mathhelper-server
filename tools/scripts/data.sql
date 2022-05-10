-- indexes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- TASKSETS --
DROP INDEX IF EXISTS idx_fts_taskset;
CREATE INDEX IF NOT EXISTS idx_keywords_taskset ON public.taskset USING gin(make_tsvector_by_keywords(keywords));

CREATE TABLE IF NOT EXISTS public.taskset_word AS SELECT word FROM ts_stat('SELECT to_tsvector(''simple'', keywords) FROM public.taskset');

CREATE INDEX IF NOT EXISTS taskset_word_idx ON taskset_word USING GIN (word gin_trgm_ops);

drop trigger if exists trig_add_taskset_word on public.taskset;

CREATE TRIGGER trig_add_taskset_word
    AFTER INSERT ON public.taskset
    FOR EACH ROW
EXECUTE PROCEDURE function_add_taskset_word();

-- TASKS --
DROP INDEX IF EXISTS idx_fts_task;
CREATE INDEX IF NOT EXISTS idx_keywords_task ON public.task USING gin(make_tsvector_by_keywords(keywords));

CREATE TABLE IF NOT EXISTS public.task_word AS SELECT word FROM ts_stat('SELECT to_tsvector(''simple'', keywords) FROM public.task');

CREATE INDEX IF NOT EXISTS task_word_idx ON task_word USING GIN (word gin_trgm_ops);

drop trigger if exists trig_add_task_word on public.task;

CREATE TRIGGER trig_add_task_word
    AFTER INSERT ON public.task
    FOR EACH ROW
EXECUTE PROCEDURE function_add_task_word();

-- RULE PACKS --
DROP INDEX IF EXISTS idx_fts_rule_pack;
CREATE INDEX IF NOT EXISTS idx_keywords_rule_pack ON public.rule_pack USING gin(make_tsvector_by_keywords(keywords));

CREATE TABLE IF NOT EXISTS public.rule_pack_word AS SELECT word FROM ts_stat('SELECT to_tsvector(''simple'', keywords) FROM public.rule_pack');

CREATE INDEX IF NOT EXISTS rule_pack_word_idx ON rule_pack_word USING GIN (word gin_trgm_ops);

drop trigger if exists trig_add_rule_pack_word on public.rule_pack;

CREATE TRIGGER trig_add_rule_pack_word
    AFTER INSERT ON public.rule_pack
    FOR EACH ROW
EXECUTE PROCEDURE function_add_rule_pack_word();
