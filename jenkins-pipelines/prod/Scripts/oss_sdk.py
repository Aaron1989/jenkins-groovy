# -*- coding: utf-8 -*-
import oss2
import sys

def war_exist(x):
    object_exist = bucket.object_exists(x)
    if object_exist:
        return True
    else:
        return False

if __name__ == "__main__":
    bucket_name = sys.argv[1]
    path = sys.argv[2]
    old_version = sys.argv[3]
    new_version = sys.argv[4]
    war_name = sys.argv[5]
    AccessKeyId = sys.argv[6]
    AccessKeySecret = sys.argv[7]

    auth = oss2.Auth(AccessKeyId, AccessKeySecret)
    service = oss2.Service(auth, 'oss-cn-qingdao.aliyuncs.com')
    bucket = oss2.Bucket(auth, 'http://oss-cn-qingdao.aliyuncs.com', bucket_name)

    if path == '/':
        oldwar = old_version + '/' + war_name
        newar = new_version + '/' + war_name
    else:
        oldwar = path + '/' + old_version + '/' + war_name
        newar = path + '/' + new_version + '/' + war_name

    obj = [oldwar,newar]


    for i in obj:
        return_result = war_exist(i)
        if return_result == True:
            if i == oldwar:
                print i + ' exist'
            else:
                print 'ERROR ' + i + ' should not exist'
                sys.exit(1)
        else:
            if i == oldwar:
                if old_version != 'no':
                    print 'ERROR ' + i +  ' dose not exist'
                    sys.exit(1)
            else:
                print i + ' not exist'



         
       
