/**
 * Created by huis on 2017/1/22.
 */

def type = params.type

node('master') {
    if (!type) {
        def userInput = input(
                id: 'userInput', message: '请输入构建参数?', parameters: [
                [$class: 'StringParameterDefinition', defaultValue: '', name: 'type', description: '发布类型：rls或hfx'],
        ])
        print userInput
        type = userInput
    }
    stage('deploy') {
        build(job: 'nec-web-upload', parameters: [
                string(name: 'src_image', value: "nec-admin-web:${type}"),
                string(name: 'bucket', value: 'nec-prd-admin'),
                string(name: 'project_type', value: 'admin'),
                string(name: 'deploy_env', value: 'prd')])

        build(job: 'nec-web-upload', parameters: [
                string(name: 'src_image', value: "nec-exam-web:${type}"),
                string(name: 'bucket', value: 'nec-prd-pc'),
                string(name: 'project_type', value: 'pc'),
                string(name: 'deploy_env', value: 'prd')])

        build(job: 'nec-web-upload', parameters: [
                string(name: 'src_image', value: "nec-mobile:${type}"),
                string(name: 'bucket', value: 'nec-prd-mobile'),
                string(name: 'project_type', value: 'mobile'),
                string(name: 'deploy_env', value: 'prd')])

        build(job: 'nec-web-upload', parameters: [
                string(name: 'src_image', value: "nec-mgt-web:${type}"),
                string(name: 'bucket', value: 'nec-prd-pc'),
                string(name: 'project_type', value: 'mgt'),
                string(name: 'deploy_env', value: 'prd'),
                string(name: 'oss_prefix', value: 'mgt')])

        build(job: 'nec-web-upload', parameters: [
                string(name: 'src_image', value: "nec-mgt-mobile:${type}"),
                string(name: 'bucket', value: 'nec-prd-mobile'),
                string(name: 'project_type', value: 'mgt'),
                string(name: 'deploy_env', value: 'prd'),
                string(name: 'oss_prefix', value: 'mgt')])

    }
}
