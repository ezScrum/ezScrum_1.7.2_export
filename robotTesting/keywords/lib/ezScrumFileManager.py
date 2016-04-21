import os
import glob


def get_latest_ezscrum_json_file_path_in_directory(directory):
    try:
        arg = glob.iglob(directory + '/*_ezScrum_export*.json')
        latest_ezscrum_json_file = max(arg, key=os.path.getctime)
        full_path_of_file = os.path.abspath(latest_ezscrum_json_file)
    except ValueError:
        print 'There is no ezScum JSON file'
        full_path_of_file = ''
    return full_path_of_file


def clean_ezscrum_json_file_in_directory(directory):
    files = glob.glob(directory + "\*_ezScrum_export*.json")
    for file_in_directory in files:
        try:
            os.remove(file_in_directory)
        except OSError:
            pass
