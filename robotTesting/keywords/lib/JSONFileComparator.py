import json


def is_valid_json_file(file_path):
    is_valid = True
    try:
        with open(file_path) as json_data:
            json_object = json.load(json_data)
    except ValueError:
        is_valid = False
    return is_valid


def is_json_array_contains_json_object(json_array, json_object):
    is_contains = False
    for index in range(len(json_array)):
        if json_array[index] == json_object:
            is_contains = True
    return is_contains


def delete_json_object_in_json_array(json_array, json_object):
    if len(json_array) == 0:
        return
    index_to_delete = -1
    for index in range(len(json_array)):
        if json_array[index] == json_object:
            index_to_delete = index
    if index_to_delete > -1:
        del json_array[index_to_delete]


def is_two_json_array_logically_the_same(json_array_a, json_array_b):
    is_the_same = len(json_array_a) == len(json_array_b)
    for index in range(len(json_array_a)):
        json_object = json_array_a[index]
        is_json_array_b_contains_json_object = is_json_array_contains_json_object(json_array_b, json_object)
        if is_json_array_b_contains_json_object:
            delete_json_object_in_json_array(json_array_b, json_object)
        else:
            return False
    return is_the_same


def remove_key_in_json_object(json_object, key):
    try:
        del json_object[key]
    except KeyError:
        print('The key not exist')


def verify_and_remove_history_json_array_in_json(json_a, json_b):
    key_of_histories = 'histories'
    key_of_create_time = 'create_time'
    history_json_array_in_json_a = json_a[key_of_histories]
    history_json_array_in_json_b = json_b[key_of_histories]
    if len(history_json_array_in_json_a) != len(history_json_array_in_json_b):
        return False
    for index in range(len(history_json_array_in_json_a)):
        del history_json_array_in_json_a[index][key_of_create_time]
        del history_json_array_in_json_b[index][key_of_create_time]
    if not is_two_json_array_logically_the_same(history_json_array_in_json_a, history_json_array_in_json_b):
        return False
    remove_key_in_json_object(json_a, key_of_histories)
    remove_key_in_json_object(json_b, key_of_histories)
    return True


def compare_json_file(exported_file_path, golden_answer_file_path):
    is_exported_file_valid = is_valid_json_file(exported_file_path)
    is_golden_answer_file_valid = is_valid_json_file(golden_answer_file_path)
    if not is_exported_file_valid or not is_golden_answer_file_valid:
        print('file invalid')
        return False
    with open(exported_file_path) as json_data:
        exported_json_object = json.load(json_data)
    with open(golden_answer_file_path) as json_data:
        answer_json_object = json.load(json_data)

    try:
        # check project has the key of create time in json array
        key_of_projects = 'projects'
        project_json_array_in_exported = exported_json_object[key_of_projects]
        project_json_array_in_answer = answer_json_object[key_of_projects]
        key_of_project_create_time = 'create_time'
        # check project has the key of create time in json array in exported json
        for index in range(len(project_json_array_in_exported)):
            project_json = project_json_array_in_exported[index]
            create_time_in_project_json = project_json[key_of_project_create_time]
            if type(create_time_in_project_json) is long:
                # remove create time in project json
                remove_key_in_json_object(project_json, key_of_project_create_time)
            else:
                print ('Create Time is not in the answer Project')
                return False
        # check project has the key of create time in json array in answer json
        for index in range(len(project_json_array_in_answer)):
            project_json = project_json_array_in_answer[index]
            create_time_in_project_json = project_json[key_of_project_create_time]
            if type(create_time_in_project_json) is long:
                # remove create time in project json
                remove_key_in_json_object(project_json, key_of_project_create_time)
            else:
                print ('Create Time is not in the exported Project')
                return False
        # check account json array
        key_of_accounts = 'accounts'
        account_json_array_in_exported = exported_json_object[key_of_accounts]
        account_json_array_in_answer = answer_json_object[key_of_accounts]
        is_two_account_json_array_the_same = is_two_json_array_logically_the_same(account_json_array_in_exported,
                                                                                  account_json_array_in_answer)
        if not is_two_account_json_array_the_same:
            print ('Account json array are not the same')
            return False
        # remove key of accounts
        remove_key_in_json_object(exported_json_object, key_of_accounts)
        remove_key_in_json_object(answer_json_object, key_of_accounts)

        key_of_sprints = 'sprints'
        key_of_stories = 'stories'
        key_of_tasks = 'tasks'
        key_of_unplans = 'unplans'
        key_of_dropped_stories = 'dropped_stories'
        key_of_dropped_tasks = 'dropped_tasks'

        # iterative for projects
        for index_for_project in range(len(project_json_array_in_answer)):
            project_json = project_json_array_in_answer[index_for_project]
            # iterative for dropped stories
            dropped_story_json_array = project_json[key_of_dropped_stories]
            for index_for_dropped_story in range(len(dropped_story_json_array)):
                dropped_story_json = dropped_story_json_array[index_for_dropped_story]
                exported_dropped_story_json = project_json_array_in_exported[index_for_project][key_of_dropped_stories][
                    index_for_dropped_story]
                # check projects/project/dropped_stories/story/histories
                # remove projects/project/dropped_stories/story/histories
                if not verify_and_remove_history_json_array_in_json(dropped_story_json, exported_dropped_story_json):
                    print ('Histories in Dropped Story are not the same')
                    return False
                # iterative for tasks in dropped story
                task_in_dropped_story_json_array = dropped_story_json[key_of_tasks]
                for index_for_task_in_dropped_story in range(len(task_in_dropped_story_json_array)):
                    task_in_dropped_story = task_in_dropped_story_json_array[index_for_task_in_dropped_story]
                    exported_task_in_dropped_story = exported_dropped_story_json[key_of_tasks][
                        index_for_task_in_dropped_story]
                    # check projects/project/dropped_stories/dropped_story/tasks/task/histories
                    # remove projects/project/dropped_stories/dropped_story/tasks/task/histories
                    if not verify_and_remove_history_json_array_in_json(task_in_dropped_story,
                                                                        exported_task_in_dropped_story):
                        print ('Histories in Task in Dropped Story are not the same')
                        return False
            # iterative for dropped tasks
            dropped_task_json_array = project_json[key_of_dropped_tasks]
            for index_for_dropped_task in range(len(dropped_task_json_array)):
                dropped_task_json = dropped_task_json_array[index_for_dropped_task]
                exported_dropped_task_json = project_json_array_in_exported[index_for_project][key_of_dropped_tasks][
                    index_for_dropped_task]
                # check projects/project/dropped_tasks/task/histories
                # remove projects/project/dropped_tasks/task/histories
                if not verify_and_remove_history_json_array_in_json(dropped_task_json, exported_dropped_task_json):
                    print ('Histories in Dropped Task are not the same')
                    return False
            # iterative for sprints
            sprint_json_array = project_json[key_of_sprints]
            for index_for_sprint in range(len(sprint_json_array)):
                sprint_json = sprint_json_array[index_for_sprint]
                # iterative for unplans
                unplan_json_array = sprint_json[key_of_unplans]
                for index_for_unplan in range(len(unplan_json_array)):
                    unplan_json = unplan_json_array[index_for_unplan]
                    exported_unplan_json = \
                        project_json_array_in_exported[index_for_project][key_of_sprints][index_for_sprint][
                            key_of_unplans][
                            index_for_unplan]
                    # check projects/project/sprints/sprint/unplans/unplan/histories
                    # remove projects/project/sprints/sprint/unplans/unplan/histories
                    if not verify_and_remove_history_json_array_in_json(unplan_json, exported_unplan_json):
                        print ('Histories in Unplan are not the same')
                        return False
                # iterative for stories
                story_json_array = sprint_json[key_of_stories]
                for index_for_story in range(len(story_json_array)):
                    story_json = story_json_array[index_for_story]
                    exported_story_json = \
                        project_json_array_in_exported[index_for_project][key_of_sprints][index_for_sprint][
                            key_of_stories][
                            index_for_story]
                    # check projects/project/sprints/sprint/stories/story/histories
                    # remove projects/project/sprints/sprint/stories/story/histories
                    if not verify_and_remove_history_json_array_in_json(story_json, exported_story_json):
                        print ('Histories in Story are not the same')
                        return False
                    # iterative for tasks
                    task_json_array = story_json[key_of_tasks]
                    for index_for_task in range(len(task_json_array)):
                        task_json = task_json_array[index_for_task]
                        exported_task_json = exported_story_json[key_of_tasks][index_for_task]
                        # check projects/project/sprints/sprint/stories/story/tasks/task/histories
                        # remove projects/project/sprints/sprint/stories/story/tasks/task/histories
                        if not verify_and_remove_history_json_array_in_json(task_json, exported_task_json):
                            print ('Histories in Task are not the same')
                            return False

        if exported_json_object == answer_json_object:
            print('these json files are the same')
            return True
        else:
            print('these json files are not the same')
            return False
    except KeyError:
        return False
