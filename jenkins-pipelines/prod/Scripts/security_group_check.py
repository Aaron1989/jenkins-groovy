#!/usr/bin/env python
# -*- coding: utf-8 -*-


from aliyunsdkecs.request.v20140526 import DescribeInstancesRequest
from aliyunsdkcore import client
import json
import sys

ak = 'LTAIef2xBhy3Fhn0'
secret = 'nen2ewNw4SkWdKGtsA5FbLpj8BrAfB'
region_id = 'cn-qingdao'
# group_id = 'sg-m5e2vq0s1w6w10659wjx'
group_id = sys.argv[1]

class CheckSecurityGroup(object):
    def __init__(self, group_id, ak, secret, region_id):
        self.group_id = group_id
        self.ak = ak
        self.secret = secret
        self.region_id = region_id

    @staticmethod
    def get_client(ak, secret, region_id):
        clt = client.AcsClient(ak, secret, region_id)
        return clt

    @staticmethod
    def get_request(id):
        request = DescribeInstancesRequest.DescribeInstancesRequest()
        request.set_accept_format('json')
        request.set_SecurityGroupId(id)
        return request

    @property
    def get_ecs_id(self):
        ecs_ids = []
        clt = CheckSecurityGroup.get_client(self.ak, self.secret, self.region_id)
        result = self.clt.do_action_with_exception(CheckSecurityGroup.get_request(self.group_id))
        #result = clt.do_action(CheckSecurityGroup.get_request(self.group_id))
        TotalCount = json.loads(result)['TotalCount']
        if TotalCount > 10:
            number_of_pages = (TotalCount+10-1)/10
            ecs_ids = self.get_several_pages_ecs_id(number_of_pages)
            return ecs_ids

        for item in json.loads(result)['Instances']['Instance']:
            ecs_ids.append(item['InstanceId'])

        return ecs_ids

    def get_several_pages_ecs_id(self, number_of_pages):
        ecs_ids = []
        for i in range(number_of_pages):
            clt = CheckSecurityGroup.get_client(self.ak, self.secret, self.region_id)
            req = CheckSecurityGroup.get_request(self.group_id)
            req.set_PageNumber(i+1)
            result = clt.do_action_with_exception(req)
            #result = clt.do_action(req)
            for item in json.loads(result)['Instances']['Instance']:
                ecs_ids.append(item['InstanceId'])
        return ecs_ids

def check_instances(ecs_ids, instances):
    passed = True
    for id in instances:
        if id in ecs_ids:
            print id + " in " + group_id
        else:
            print id + " not in " + group_id
            passed = False
    return passed



if __name__ == '__main__':
    check__security_group = CheckSecurityGroup(group_id, ak, secret, region_id)
    ecs_id = check__security_group.get_ecs_id
    instances = sys.argv[2:]
    # print instances
    if not check_instances(ecs_id, instances):
        sys.exit(1)

    # print ecs_id
    #
    # print sys.argv

