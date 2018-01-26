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
        a = aliyun.api.Slb20140515DescribeLoadBalancerAttributeRequest()   #实例化request
        a.LoadBalancerId = self.__loadbalancerid
        try:
            f = a.getResponse()                              #调用getResponse方法返回调用结果,如果有错误码,就抛出
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

    gray_weight = int(sys.argv[1])
    docker_weight = int(sys.argv[2])
    prod_weight = int(sys.argv[3])

    if gray_weight == 0:
        if prod_weight == 0:
            sys.exit(1)

    loadbalancerid = ['lb-m5eat9wtqdvp6olzvs4kh']
    access_key = 'LTAIpQhvkUCc6hnD'
    secret_key = 'H7AnVENkmMRqvzrLl8ta2sSLTF9uPg'
    RegionID = 'cn-qingdao'
    for m in loadbalancerid:
        slb = SLB(access_key,secret_key,m)
        count = 0
        slb.Clean()
        # for i in slb.Checkstatus(serverid=[u'i-28gvxulfo',u'i-28r9tvwho',
        #                                    u'i-28kxlnd3x',u'i-28bjlb3q0',
        #                                    u'i-28cbujys8',u'i-28c0nw3al',
        #                                    u'i-28rol1838',u'i-28sw6i3vy',
        #                                    u'i-287wjy0ty',u'i-28qmfjjtk',
        #                                    u'i-285gyz03w',u'i-28z2rfbl0',
        #                                    u'i-28ea1763n',u'i-28mxsgfdw',u'i-28i8kdzem']):
        #     if i == 0:
        #         count += 1
        # if count == 0:
            #灰度机器
        slb.SetBackendServers('i-28sp9juvj',gray_weight)
        slb.SetBackendServers('i-283xi6oau',gray_weight)
        slb.SetBackendServers('i-28td260tt',gray_weight)
        slb.SetBackendServers('i-28lue7ypu',gray_weight)
        slb.SetBackendServers('i-28hibrhyn',gray_weight)
        slb.SetBackendServers('i-28vmbv1l7',gray_weight)

        # #docker机器
        # slb.SetBackendServers('i-28rol1838',docker_weight)
        # slb.SetBackendServers('i-28sw6i3vy',docker_weight)
        # slb.SetBackendServers('i-287wjy0ty',docker_weight)
        # slb.SetBackendServers('i-28qmfjjtk',docker_weight)
        # slb.SetBackendServers('i-285gyz03w',docker_weight)
        # slb.SetBackendServers('i-28z2rfbl0',docker_weight)
        # slb.SetBackendServers('i-28ea1763n',docker_weight)
        # slb.SetBackendServers('i-28mxsgfdw',docker_weight)
        # slb.SetBackendServers('i-28i8kdzem',docker_weight)

        #生产机器
        slb.SetBackendServers('i-28pql06rw',prod_weight)
        slb.SetBackendServers('i-28hdbpc5a',prod_weight)
        slb.SetBackendServers('i-286qc70rf',prod_weight)
        slb.SetBackendServers('i-28krvd54m',prod_weight)
        slb.SetBackendServers('i-280pka3sx',prod_weight)

        slb.Clean()
        # elif m == 'lb-m5ed4o2qxkn4ntp9jgh0g':
        #     print '*.7yiyuan.com的老版本权重为0，不可以将灰度权重切为0'
        # if m == 'lb-m5ed4o2qxkn4ntp9jgh0g':
        #     print '官网灰度环境'
        # else:
        #     raise Exception
        for i in slb.CheckStatus()['BackendServers']['BackendServer']:
                # print
            if (i['ServerId']) == 'i-286qc70rf' or (i['ServerId']) == 'i-28pql06rw' or \
                        (i['ServerId']) == 'i-28hdbpc5a' or (i['ServerId']) == 'i-28krvd54m' or \
                        (i['ServerId']) == 'i-280pka3sx':
                print '老版本',
            elif (i['ServerId']) == 'i-28lue7ypu' or (i['ServerId']) == 'i-283xi6oau' or \
                        (i['ServerId']) == 'i-28sp9juvj' or (i['ServerId']) == 'i-28td260tt' or \
                        (i['ServerId']) == '-28hibrhyn' or (i['ServerId']) == 'i-28vmbv1l7':
                print '灰度',
            print i['Weight'],
        print '\n'
