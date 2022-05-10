    insert into public.user_type (code, description)
values ('test_user_type_code', 'test_user_type_description')
on conflict do nothing;

insert into public.user_entity
    (code, user_type_code, login, email, additional, name, full_name, locale, password, external_code, is_oauth, other_data)
values ('test_user_code', 'test_user_type_code', 'test_user', 'email@mail.ru', 'additional_info', 'test_user_name',
	    'test_user_full_name', 'en', 'qwerty', '', false, '{"is_president": "no", "is_student": "yes"}')
on conflict do nothing;

insert into public.subject_type
values ('test_subject_type')
on conflict do nothing;

insert into public.namespace_grant_type (code, description)
values ('PUBLIC_READ_WRITE', 'test_namespace_type_description')
on conflict do nothing;

insert into public.namespace (code, author_user_code, namespace_grant_type)
values ('test_namespace_code', 'test_user_code', 'PUBLIC_READ_WRITE')
on conflict do nothing;

insert into public.namespace (code, author_user_code, namespace_grant_type)
values ('second_test_namespace_code', 'test_user_code', 'PUBLIC_READ_WRITE')
on conflict do nothing;

insert into public.app (code, description)
values ('test_app_code', 'test_app_descr')
on conflict do nothing;

insert into public.activity_type (code, description)
values ('test_activity_type_code', 'test_activity_type_descr')
on conflict do nothing;

insert into public.rule_pack (code, namespace_code, author_user_code, name_en, name_ru, rules, other_data)
values ('test_rule_pack', 'test_namespace_code', 'test_user_code', 'test_rule_pack_en', 'test_rule_pack_ru',
        '[{"left":"x + 1", "right":"1 + x", "param":true}]', null)
on conflict do nothing;

insert into public.rule_pack_history
    (code, version, namespace_code, author_user_code, name_en, name_ru, rules, other_data, is_active, active_date_from, active_date_to)
values ('test_rule_pack', 0, 'test_namespace_code', 'test_user_code', 'test_rule_pack_en', 'test_rule_pack_ru',
        '[{"left":"x + 1", "right":"1 + x", "param":true}]', null, true,
        '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.rule_pack (code, namespace_code, author_user_code, name_en, name_ru, rules, other_data)
values ('test_rule_pack_2', 'test_namespace_code', 'test_user_code', 'test_rule_pack_2_en', 'test_rule_pack_2_ru',
        '[{"left":"x^1", "right":"x"}]', '{"some":"body"}')
on conflict do nothing;

insert into public.rule_pack_history
    (code, version, namespace_code, author_user_code, name_en, name_ru, rules, other_data, is_active, active_date_from, active_date_to)
values ('test_rule_pack_2', 0, 'test_namespace_code', 'test_user_code', 'test_rule_pack_2_en', 'test_rule_pack_2_ru',
        '[{"left":"x^1", "right":"x"}]', '{"some":"body"}', true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.rule_pack_to_rule_pack (id, parent_rule_pack_code, child_rule_pack_code)
values (0, 'test_rule_pack', 'test_rule_pack_2')
on conflict do nothing;

insert into public.rule_pack_to_rule_pack_history
    (id, parent_rule_pack_code, parent_rule_pack_version, child_rule_pack_code, child_rule_pack_version, is_active, active_date_from, active_date_to)
values (0, 'test_rule_pack', 0, 'test_rule_pack_2', 0, true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.taskset
    (code, namespace_code, name_en, name_ru, author_user_code, recommended_by_community, access_start_time, access_end_time, other_data)
values ('test_taskset_code', 'test_namespace_code', 'test_taskset_name_en', 'test_taskset_name_ru', 'test_user_code',
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00', null)
on conflict do nothing;

insert into public.taskset_history
    (code, version, namespace_code, name_en, name_ru, author_user_code, recommended_by_community, access_start_time, access_end_time, other_data, is_active, active_date_from, active_date_to)
values ('test_taskset_code', 0, 'test_namespace_code', 'test_taskset_name_en', 'test_taskset_name_ru', 'test_user_code',
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00', null, true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.taskset
    (code, namespace_code, name_en, name_ru, author_user_code, recommended_by_community, access_start_time, access_end_time, other_data)
values ('test_taskset_2_code', 'test_namespace_code', 'test_taskset_2_name_en', 'test_taskset_2_name_ru', 'test_user_code',
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00', null)
on conflict do nothing;

insert into public.taskset_history
    (code, version, namespace_code, name_en, name_ru, author_user_code, recommended_by_community, access_start_time, access_end_time, other_data, is_active, active_date_from, active_date_to)
values ('test_taskset_2_code', 0, 'test_namespace_code', 'test_taskset_2_name_en', 'test_taskset_2_name_ru', 'test_user_code',
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00', null, true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.task
    (code, namespace_code, name_en, name_ru, author_user_code,
    access_start_time, access_end_time,
    original_expression_plain_text, original_expression_tex, original_expression_structure_string,
    goal_type, goal_expression_plain_text, goal_expression_tex, goal_expression_structure_string,
    goal_pattern, steps_number, time, difficulty, solution, count_of_auto_generated_tasks)
values ('test_task_code', 'test_namespace_code', 'test_task_name_en', 'test_task_name_ru', 'test_user_code',
        '2020-11-11 00:00:00', '294270-01-01 00:00:00',
        'x+1', 'x+1', '(+(x;1))',
        'SIMPLIFICATION', 'y+2', 'y+2', '(+(y;2))',
        '', 27, 120, 0.5, '{}', 0)
on conflict do nothing;

insert into public.task_history
    (code, version, namespace_code, name_en, name_ru, author_user_code,
    access_start_time, access_end_time,
    original_expression_plain_text, original_expression_tex, original_expression_structure_string,
    goal_type, goal_expression_plain_text, goal_expression_tex, goal_expression_structure_string,
    goal_pattern, steps_number, time, difficulty, solution, count_of_auto_generated_tasks,
    is_active, active_date_from, active_date_to)
values ('test_task_code', 0, 'test_namespace_code', 'test_task_name_en', 'test_task_name_ru', 'test_user_code',
        '2020-11-11 00:00:00', '294270-01-01 00:00:00',
        'x+1', 'x+1', '(+(x;1))',
        'SIMPLIFICATION', 'y+2', 'y+2', '(+(y;2))',
        '', 27, 120, 0.5, '{}', 0,
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;

insert into public.task
    (code, namespace_code, name_en, name_ru, author_user_code,
    access_start_time, access_end_time,
    original_expression_plain_text, goal_type, goal_expression_plain_text, goal_pattern,
    steps_number, time, difficulty, solution, count_of_auto_generated_tasks)
values ('test_task_2_code', 'test_namespace_code', 'test_task_2_name_en', 'test_task_2_name_ru', 'test_user_code',
        '2020-11-11 00:00:00', '294270-01-01 00:00:00',
        'sdcearca', 'DNF', 'earvaervaerv', '',
        12, 1200, 5.5, '{}', 0)
on conflict do nothing;

insert into public.task_history
    (code, version, namespace_code, name_en, name_ru, author_user_code,
    access_start_time, access_end_time,
    original_expression_plain_text, goal_type, goal_expression_plain_text, goal_pattern,
    steps_number, time, difficulty, solution, count_of_auto_generated_tasks,
    is_active, active_date_from, active_date_to)
values ('test_task_2_code', 0, 'test_namespace_code', 'test_task_2_name_en', 'test_task_2_name_ru', 'test_user_code',
        '2020-11-11 00:00:00', '294270-01-01 00:00:00',
        'sdcearca', 'DNF', 'earvaervaerv', '',
        12, 1200, 5.5, '{}', 0,
        true, '2020-11-11 00:00:00', '294270-01-01 00:00:00')
on conflict do nothing;
