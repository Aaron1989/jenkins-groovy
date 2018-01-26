#coding=utf-8
import aliyun.api
import sys
reload(sys)                                                     #python2.7不区分unicode和bytes,认为都是str,所以处理中文很麻烦,除非知道enconde时候的编码,才能对应decode。所以1开始就默认重写了sys的编码
sys.setdefaultencoding( "utf-8" )

class SLB:
    def __init__(self,access_key,secret_key,loadbalancerid):
        '''
        :private param access_key:
        :private param secret_key:
        :public param loadbalancerid:
        :list param serverid,weight：
        :return:
        '''
        self.__access_key = access_key
        self.__secret_key = secret_key
        self.__loadbalancerid = loadbalancerid
        self.serverid = []
        self.weight = []
        aliyun.setDefaultAppInfo(self.__access_key,self.__secret_key)

    def Clean(self):
        '''
        :reset list params
        :return:
        '''
        self.serverid = []
        self.weight = []

    def GetInfo(self):
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()   #实例化request
        a.LoadBalancerId = self.__loadbalancerid
        return a.getResponse()                                    #调用getResponse方法返回调用结果


    def GetServerId(self):
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()   #实例化request
        a.LoadBalancerId = self.__loadbalancerid
        try:
            f = a.getResponse()                              #调用getResponse方法返回调用结果,如果有错误码,就抛出
            if ('Code' in f):
                print('f.code:{0},f.message:{0}'.format(f['Code'],f['Message']))
            else:
                for i in f['BackendServers']['BackendServer']:
                    self.serverid.append(i['ServerId'])
                return self.serverid
        except Exception as e:
            raise e

    def GetWeight(self):
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()   #实例化request
        a.LoadBalancerId = self.__loadbalancerid
        try:
            f = a.getResponse()                              #调用getResponse方法返回调用结果,如果有错误码,就抛出
            if ('Code' in f):
                print('f.code:{0},f.message:{0}'.format(f['Code'],f['Message']))
            else:
                for i in f['BackendServers']['BackendServer']:
                    self.weight.append(i['Weight'])
                return self.weight
        except Exception as e:
            raise e

    def Checkstatus(self,serverid):
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()   #实例化request
        a.LoadBalancerId = self.__loadbalancerid
        try:
            f = a.getResponse()                              #调用getResponse方法返回调用结果,如果有错误码,就抛出
            if ('Code' in f):
                print('f.code:{0},f.message:{0}'.format(f['Code'],f['Message']))
            else:
                status = []
                for i in f['BackendServers']['BackendServer']:
                    if i[u'ServerId'] not in serverid:
                        status.append(i[u'Weight'])
                return status
        except Exception as e:
            raise e

    def CheckStatus(self):
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()  # 实例化request
        a.LoadBalancerId = self.__loadbalancerid
        try:
            f = a.getResponse()  # 调用getResponse方法返回调用结果,如果有错误码,就抛出
            return f
        except Exception as e:
            raise e

    def SetBackendServers(self,serverid,weigth):
        '''
        :初始化基类
        :Args @public dict param BackendServers: 后端ecs(需要传入serverid和weigth组成的字典)
        :      @public param LoadBalancerId: slb的id
        '''
        a = aliyun.api.Slb20140515SetBackendServersRequest()      #实例化request
        a.BackendServers = [{'ServerId':serverid,'Weight':weigth}]
        a.LoadBalancerId = self.__loadbalancerid
        a.getResponse()


if __name__ == '__main__':
    loadbalancerid = ['lb-m5ed4o2qxkn4ntp9jgh0g']
    access_key = 'LTAIpQhvkUCc6hnD'
    secret_key = 'H7AnVENkmMRqvzrLl8ta2sSLTF9uPg'
    RegionID = 'cn-qingdao'
    for m in loadbalancerid:
        slb = SLB(access_key,secret_key,m)
        slb.SetBackendServers('i-28cq99swx',100)
        slb.SetBackendServers('i-28cl7r4pk',100)
        slb.SetBackendServers('i-m5e9ulw448ikjxfvyw8n',100)
        slb.SetBackendServers('i-m5e9ulw448ikjxfvyw8m',100)
        slb.SetBackendServers('i-28wcjlql1', 100)
        slb.SetBackendServers('i-28nfaeiij', 100)
        slb.Clean()
        if m == 'lb-m5ed4o2qxkn4ntp9jgh0g':
            print '官网灰度环境'
        else:
            raise Exception
        for i in slb.CheckStatus()['BackendServers']['BackendServer']:
            # print i
            if (i['ServerId']) == 'i-28cl7r4pk' or (i['ServerId']) == 'i-28cq99swx' \
                    or (i['ServerId']) == 'i-m5e9ulw448ikjxfvyw8m' or (i['ServerId']) == 'i-m5e9ulw448ikjxfvyw8n' \
                    or (i['ServerId']) == 'i-28wcjlql1' or (i['ServerId']) == 'i-28nfaeiij':
                print '老版本',
            elif (i['ServerId']) == 'i-28r9tvwho' or (i['ServerId']) == 'i-28gvxulfo' \
                    or (i['ServerId']) == 'i-28kxlnd3x' or (i['ServerId']) == '-28bjlb3q0' \
                    or (i['ServerId']) == 'i-28cbujys8' or (i['ServerId']) == 'i-28c0nw3al':
                print '灰度',
            elif (i['ServerId']) == 'i-28rol1838' or (i['ServerId']) == 'i-28sw6i3vy' \
                    or (i['ServerId']) == 'i-287wjy0ty' or (i['ServerId']) == 'i-28qmfjjtk' \
                    or (i['ServerId']) == 'i-285gyz03w' or (i['ServerId']) == 'i-28z2rfbl0' \
                    or (i['ServerId']) == 'i-28ea1763n' or (i['ServerId']) == 'i-28mxsgfdw' \
                    or (i['ServerId']) == 'i-28i8kdzem':
                print '灰度docker',
            print i['Weight'],
        print '\n'