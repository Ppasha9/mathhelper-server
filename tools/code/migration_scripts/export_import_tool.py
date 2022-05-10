"""
Tool for fast exporting/importing rule-packs and tasksets from/to MathHelper server

Usage:
    export_import_tool.py --import [--rule-packs=FILE_PATH] [--tasksets=FILE_PATH] [--debug]
    export_import_tool.py --export (--namespace=NAMESPACE) ((--all --output-dir=OUTPUT_DIR) | ([--rule-packs-output=FILE_PATH] [--tasksets-output=FILE_PATH])) [--debug]
    export_import_tool.py -h | --help

Options:
    --import                        This command tells to the tool that we want to import some data.
    --export                        This command tells to the tool that we want to export some data.
    --rule-packs=FILE_PATH          Full path to the JSON file that contains rule-packs' infos to import.
                                    File content should be like this:
                                    {
                                        "rulePacks": [
                                            {
                                                "namespace": ...
                                                "nameRu": ...
                                                "code": ...
                                                ...
                                            },
                                            ...
                                        ]
                                    }
    --tasksets=FILE_PATH            Full path to the JSON file that contains tasksets' infos to import.
                                    File content should be like this:
                                    {
                                        "taskSets": [
                                            {
                                                "namespace": ...
                                                "nameRu": ...
                                                "code": ...
                                                ...
                                            },
                                            ...
                                        ]
                                    }
    --namespace=NAMESPACE           Namespace code, whose rule-packs or tasksets we should export.
    --rule-packs-output=FILE_PATH   Full path to the file, that will contain all exported rule-packs' infos.
    --tasksets-output=FILE_PATH     Full path to the file, that will contain all exported tasksets' infos.
    --all                           This command means that we should export rule-packs and tasksets.
    --output-dir=OUTPUT_DIR         Full path to the directory where will be created files with exported rule-packs' and tasksets' infos.
    --debug                         Turn on DEBUG logging.
    -h --help                     Show this message.
"""

import os
import sys
import json
import docopt
import logging
import requests

from requests.exceptions import HTTPError

_log = logging.getLogger("mathhelperserver.export_import_tool")

_SERVER_HOST = "https://mathhelper.space:8089"
# _SERVER_HOST = "http://localhost:8089"
_SIGNIN_URL = _SERVER_HOST + "/api/auth/signin/"
_RULE_PACKS_URL = _SERVER_HOST + "/api/rule-pack/"
_TASKSETS_URL = _SERVER_HOST + "/api/taskset/"
_RULE_PACKS_GET_ONE_URL = _RULE_PACKS_URL
_TASKSETS_GET_ONE_URL = _TASKSETS_URL + "edit/"

_AUTH_TOKEN = ""


def _export_entities(what_entities_to_export, namespace, output_file) -> bool:
    """
    Common function that exports all entities `:what_entities_to_export` by namespace `:namespace` to the `:output_file`
    """
    _log.debug(f"Exporting {what_entities_to_export} with namespace `{namespace}` to `{output_file}`...")

    try:
        _log.debug(f"Fetching {what_entities_to_export} from server...")
        url = _RULE_PACKS_URL if what_entities_to_export == "rulePacks" else _TASKSETS_URL
        response = requests.get(url, params={'namespace': namespace})
        response.raise_for_status()
        response_json = response.json()
    except HTTPError as e:
        _log.error(f'HTTP error occurred while exporting {what_entities_to_export} with namespace `{namespace}`. Exception: {e}')
        return False
    except Exception as e:
        _log.error(f'Exception occurred while exporting {what_entities_to_export} with namespace `{namespace}`. Exception: {e}')
        return False

    if os.path.exists(output_file):
        os.remove(output_file)

    _log.debug(f"Saving fetched {what_entities_to_export} to `{output_file}`...")
    with open(output_file, "w", encoding="utf8") as f:
        list_to_save = response_json if what_entities_to_export == "rulePacks" else response_json["tasksets"]
        json.dump({what_entities_to_export: list_to_save}, f, indent=4, ensure_ascii=False)

    return True


def _export_rule_packs(namespace, output_file) -> bool:
    """
    Exports all rule packs by namespace `:namespace` to the `:output_file`
    """
    return _export_entities("rulePacks", namespace, output_file)


def _export_tasksets(namespace, output_file) -> bool:
    """
    Exports all tasksets by namespace `:namespace` to the `:output_file`
    """
    return _export_entities("taskSets", namespace, output_file)


def _perform_export(opts):
    namespace_code = opts['--namespace']

    def __handle_export_rule_packs(output_file):
        export_res = _export_rule_packs(namespace_code, output_file)
        if not export_res:
            _log.error(f"Failed to export rule-packs with namespace `{namespace_code}` to `{output_file}`")

    def __handle_export_tasksets(output_file):
        export_res = _export_tasksets(namespace_code, output_file)
        if not export_res:
            _log.error(f"Failed to export tasksets with namespace `{namespace_code}` to `{output_file}`")

    if opts['--all']:
        output_dir = opts['--output-dir']
        os.makedirs(output_dir, exist_ok=True)

        rule_packs_output_file = os.path.join(output_dir, "rule_packs.json")
        __handle_export_rule_packs(rule_packs_output_file)

        tasksets_output_file = os.path.join(output_dir, "tasksets.json")
        __handle_export_tasksets(tasksets_output_file)
    else:
        if opts['--rule-packs-output']:
            rule_packs_output_file = opts['--rule-packs-output']
            os.makedirs(rule_packs_output_file, exist_ok=True)
            __handle_export_rule_packs(rule_packs_output_file)

        if opts['--tasksets-output']:
            tasksets_output_file = opts['--tasksets-output']
            os.makedirs(tasksets_output_file, exist_ok=True)
            __handle_export_tasksets(tasksets_output_file)


def _retrieve_auth_token():
    try:
        _log.debug("Retrieving auth token...")
        response = requests.post(_SIGNIN_URL, json={"loginOrEmail": "mathhelper", "password": "mathhelper"})
        response.raise_for_status()
    except HTTPError as e:
        _log.error(f"HTTP error occurred while retrieving auth token. Exception: {e}")
        return
    except Exception as e:
        _log.error(f"Exception occurred while retrieving auth token. Exception: {e}")
        return

    global _AUTH_TOKEN
    _AUTH_TOKEN = response.json()["token"]


def _check_entity_existence_by_code(what_entities_to_check, entity_code) -> bool:
    global _AUTH_TOKEN
    try:
        _log.debug(f'Checking entity existence by code `{entity_code}`')
        url = (_RULE_PACKS_GET_ONE_URL if what_entities_to_check == "rulePacks" else _TASKSETS_GET_ONE_URL) + entity_code
        response = requests.get(url, headers={"Authorization": f"Bearer {_AUTH_TOKEN}"})
        if response.status_code == 404:
            return False
        response.raise_for_status()
    except HTTPError as e:
        _log.error(f'HTTP error occurred while checking entity existence by code `{entity_code}`. Exception: {e}')
        return False
    except Exception as e:
        _log.error(f'Exception occurred while checking entity existence by code `{entity_code}`. Exception: {e}')
        return False

    return True


def _import_entities(what_entities_to_export, infos_file) -> bool:
    """
    Importing all entities `:what_entities_to_export` from `:infos_file` to server.
    """
    if not os.path.exists(infos_file):
        _log.error(f"Failed to import {what_entities_to_export}. File {infos_file} doesn't exist")
        return False

    _log.debug(f"Reading {what_entities_to_export} from `{infos_file}`")
    with open(infos_file, "r", encoding="utf8") as f:
        data = json.load(f)

    entities = data[what_entities_to_export]
    global _AUTH_TOKEN
    for entity in entities:
        try:
            _log.debug(f'Importing entity with code `{entity["code"]}`')
            is_exist = _check_entity_existence_by_code(what_entities_to_export, entity["code"])
            url = (_RULE_PACKS_URL if what_entities_to_export == "rulePacks" else _TASKSETS_URL) +\
                  ("create/" if not is_exist else "update/")
            response = requests.post(url, json=entity, headers={"Authorization": f"Bearer {_AUTH_TOKEN}"})
            response.raise_for_status()
        except HTTPError as e:
            _log.error(f'HTTP error occurred while import entity with code `{entity["code"]}`. Exception: {e}')
            return False
        except Exception as e:
            _log.error(f'Exception occurred while import entity with code `{entity["code"]}`. Exception: {e}')
            return False

    return True


def _import_rule_packs(rule_packs_infos_file) -> bool:
    """
    Importing all rule packs from `:rule_packs_infos_file` to server.
    """
    return _import_entities("rulePacks", rule_packs_infos_file)


def _import_tasksets(tasksets_infos_file) -> bool:
    """
    Importing all tasksets from `:tasksets_infos_file` to server.
    """
    return _import_entities("taskSets", tasksets_infos_file)


def _perform_import(opts):
    _retrieve_auth_token()

    if opts['--rule-packs']:
        import_res = _import_rule_packs(opts['--rule-packs'])
        if not import_res:
            _log.error(f"Failed to import rule-packs from file {opts['--rule-packs']}")

    if opts['--tasksets']:
        import_res = _import_tasksets(opts['--tasksets'])
        if not import_res:
            _log.error(f"Failed to import tasksets from file {opts['--tasksets']}")


def _setup_logger(opts):
    stdout_handler = logging.StreamHandler(sys.stdout)
    if opts['--debug']:
        _log.setLevel(logging.DEBUG)
        stdout_handler.setLevel(logging.DEBUG)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    stdout_handler.setFormatter(formatter)
    _log.addHandler(stdout_handler)


def _main(opts):
    _setup_logger(opts)

    if opts['--export']:
        _perform_export(opts)
    elif opts['--import']:
        _perform_import(opts)


if __name__ == "__main__":
    _main(docopt.docopt(__doc__))
