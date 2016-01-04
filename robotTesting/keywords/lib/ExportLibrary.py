# coding=UTF-8
import MySQLdb
import warnings
import shutil
import os

ezscrum_web_content = "/WebContent"
workspace = "/Workspace"
role_base = "/RoleBase.xml"
pure_web_content = "/robotTesting/TestData/Pure_WebContent"
test_data_web_content = "/robotTesting/TestData/WebContent_With_Test_Data"
test_data_sql_file_name = "/robotTesting/TestData/golden_answer_dump.sql"


class ExportLibrary:
    def __init__(self):
        pass

    """ 將Test Data覆蓋到目標 """
    def export_setup(self, ezscrum_directory, builded_directory):
        # Delete 目標資料
        try:
            shutil.rmtree(builded_directory + ezscrum_web_content + workspace)
            os.remove(builded_directory + ezscrum_web_content + role_base)
        except WindowsError:
            print("刪除失敗")
        except OSError:
            print("刪除失敗")

        # 複製檔案到目標
        try:
            shutil.copytree(ezscrum_directory + test_data_web_content + workspace, builded_directory + ezscrum_web_content + workspace)
            shutil.copy(ezscrum_directory + test_data_web_content + role_base, builded_directory + ezscrum_web_content + role_base)
        except OSError, e: # python >2.5
            print '找不到此檔案或檔案路徑: %s' % e

    """ 將Test Data清除 """
    def export_teardown(self, ezscrum_directory, builded_directory):
        # Delete 目標資料
        try:
            shutil.rmtree(builded_directory + ezscrum_web_content + workspace)
            os.remove(builded_directory + ezscrum_web_content + role_base)
        except WindowsError:
            print("刪除失敗")
        except OSError:
            print("刪除失敗")
        # 複製檔案到目標
        try:
            shutil.copytree(ezscrum_directory + pure_web_content + workspace, builded_directory + ezscrum_web_content + workspace)
            shutil.copy(ezscrum_directory + pure_web_content + role_base, builded_directory + ezscrum_web_content + role_base)
        except OSError, e: # python >2.5
            print '找不到此檔案或檔案路徑: %s' % e

    def execute_scripts_from_file(self, host_url, account, password, databaseName, ezscrum_directory):
        # Open and read the file as a single buffer
        fd = open(ezscrum_directory + test_data_sql_file_name, 'r')
        sqlFile = fd.read()
        fd.close()

        # all SQL commands (split on ';')
        sqlCommands = sqlFile.split(';')
        db = MySQLdb.connect(host=host_url, user=account, passwd=password, db=databaseName, charset='utf8')
        cursor = db.cursor()

        # ignore useless warnings
        warnings.filterwarnings('ignore', 'Unknown table .*')
        warnings.filterwarnings('ignore', 'Changing sql mode*')

        # Execute every command from the input file
        for command in sqlCommands:
            try:
                if command:
                    cursor.execute(command)
            except MySQLdb.OperationalError:
                print "Command error: " + command
            except MySQLdb.Warning:
                print "Command warning: " + command
