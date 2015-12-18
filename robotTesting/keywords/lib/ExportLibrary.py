# coding=UTF-8
import MySQLdb
import shutil
import os

testData = "C:/Users/ibmboy/Desktop/Export_Acceptance_Test_Data/WebContent_With_Test_Data"
pureWorkspace = "C:/Users/ibmboy/Desktop/Export_Acceptance_Test_Data/Pure_WebContent"
dst = "C:/Users/ibmboy/Desktop/WebContent"
workspace = "Workspace"
roleBase = "RoleBase.xml"

class ExportLibrary:
    def __init__(self):
        pass

    """ 將Test Data覆蓋到目標 """
    def export_setup(self):
        # Delete 目標資料
        try:
            shutil.rmtree(dst + "/" + workspace)
            os.remove(dst + "/" + roleBase)
        except WindowsError:
            print("刪除失敗")
        except OSError:
            print("刪除失敗")

        # 複製檔案到目標
        try:
            shutil.copytree(testData + "/" + workspace, dst + "/" + workspace)
            shutil.copy(testData + "/" + roleBase, dst + "/" + roleBase)
        except OSError as exc: # python >2.5
            if exc.errno == errno.ENOTDIR:
                shutil.copy(src, dst)
            else: raise

    """ 將Test Data清除 """
    def export_teardown(self):
        # Delete 目標資料
        try:
            shutil.rmtree(dst + "/" + workspace)
            os.remove(dst + "/" + roleBase)
        except WindowsError:
            print("刪除失敗")
        except OSError:
            print("刪除失敗")
        # 複製檔案到目標
        try:
            shutil.copytree(pureWorkspace + "/" + workspace, dst + "/" + workspace)
            shutil.copy(pureWorkspace + "/" + roleBase, dst + "/" + roleBase)
        except OSError as exc: # python >2.5
            if exc.errno == errno.ENOTDIR:
                shutil.copy(src, dst)
            else: raise

    def executeScriptsFromFile(self, hostUrl, account, password, databaseName):
        # Open and read the file as a single buffer
        fd = open(filename, 'r')
        sqlFile = fd.read()
        fd.close()

        # all SQL commands (split on ';')
        sqlCommands = sqlFile.split(';')
        db = MySQLdb.connect(host=hostUrl, user=account, passwd=password, db=databaseName)
        cursor = db.cursor()

        # Execute every command from the input file
        for command in sqlCommands:
            try:
                cursor.execute(command)
            except OperationalError, msg:
                print "Command skipped: ", msg
        db.commit()
