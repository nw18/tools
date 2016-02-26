#!/bin/bash
ECONDS=0

#假设脚本放置在与项目相同的路径下
project_path=$(pwd)
#取当前时间字符串添加到文件结尾
now=`python get_version.py`

#指定项目的scheme名称
scheme="iYJ"
#指定要打包的配置名
if [ x$1 != x ]
then
    configuration=$1
else
    configuration="Release"
fi

#指定打包所使用的provisioning profile名称
provisioning_profile='Provisioning_dev_workbook.mobileprovision'

#指定项目地址
workspace_path="$project_path/iYJ.xcworkspace"
#指定输出路径
output_path="$project_path/output2"
#指定输出归档文件地址
archive_path="$output_path/iYJ_${now}.xcarchive"
#指定输出ipa地址
ipa_path="$output_path/iYJ_${now}.ipa"
#获取执行命令时的commit message
commit_msg="$1"

#输出设定的变量值
echo "===workspace path: ${workspace_path}==="
echo "===archive path: ${archive_path}==="
echo "===ipa path: ${ipa_path}==="
echo "===profile: ${provisioning_profile}==="
echo "===commit msg: $1==="

export PATH=/usr/local/bin:${PATH}
#先清空前一次build
#xctool clean -workspace ${workspace_path} -scheme ${scheme} -configuration ${configuration}
security unlock-keychain -p "Huhuiqing0909" "/Users/hzf/Library/Keychains/login.keychain"
#根据指定的项目、scheme、configuration与输出路径打包出archive文件
xctool clean build -workspace ${workspace_path} -scheme ${scheme} -configuration ${configuration} archive -archivePath ${archive_path}

#使用指定的provisioning profile导出ipa
#我暂时没找到xctool指定provisioning profile的方法，所以这里用了xcodebuild
xcodebuild -exportArchive -archivePath ${archive_path} -exportPath ${ipa_path} -exportFormat ipa -exportSigningIdentity "iPhone Developer: zhao pan (VDEHQRC2S4)"
#-exportProvisioningProfile "${provisioning_profile}"

#上传到fir
#fir publish ${ipa_path} -T fir_token -c "${commit_msg}"

#输出总用时
echo "===Finished. Total time: ${SECONDS}s==="
