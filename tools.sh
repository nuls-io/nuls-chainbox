#!/bin/bash
#定义帮助函数
#检查运行环境，JDK,MAVEN
#定义获取NULS2.0运行环境函数
#定义获取模板函数
#定义打包函数

cd `dirname $0`

NULS2_REPO="git@github.com:nuls-io/nuls-v2.git"
NULS2_BRANCH="develop"
NULS2_RUNTIME_DIR="NULS-WALLET-RUNTIME"
NULS2_REPO_DIR=".NULS2.0"

JAVA_MODULE_TEMP_REPO="git@github.com:lijunzhou/nuls-java-module-template.git"

TEMPLATLIST="java"

help()
{
    cat <<- EOF
    Desc: NULS-engine 脚本程序
    Usage: ./tools
    		-t <language> [folder name] 获取指定开发语言的模块程序模块 
                <language> 开发语言 ，如java
                [folder name] 下载到指定名字的目录下
            -l 查看支持的模板列表    
    		-n 获取NULS2.0运行环境(从nuls-v2/nuls-engine分支拉)
    		-p <模块目录>   集成指定模块到NULS2.0运行环境中
            -a 添加一个基础模块
            -r 移除一个基础模块
            -s 查看配置的待打包的基础模块列表            
    		-h 查看帮助
    		
EOF
    exit 0
}

getModuleItem(){
    while read line
    do
        pname=`echo $line | awk -F '=' '{print $1}'`
        pvalue=`awk -v a="$line" '
                        BEGIN{
                            len = split(a,ary,"=")
                            r=""
                            for ( i = 2; i <= len; i++ ){
                                if(r != ""){
                                    r = (r"=")
                                }
                                r=(r""ary[i])
                            }
                            print r
                        }
                    '`
        if [ "${pname}" == $2 ]; then
            echo ${pvalue};
            return 1;
        fi
    done < $1
    return 0
}


#日志打印函数
echoRed() { echo $'\e[0;31m'$1$'\e[0m'; } #print red
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; } #print green
echoYellow() { echo $'\e[0;33m'$1$'\e[0m'; } #print yellow
log(){ #print date prefix and green
    now=`date "+%Y-%m-%d %H:%M:%S"`
    echoGreen "$now $@"
}

# 检查java版本 must be 11
checkJavaVersion(){
    JAVA="$JAVA_HOME/bin/java"
    if [ ! -r "$JAVA" ]; then
        JAVA='java'
    fi

    JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
    if [ ! -n "$JAVA_EXIST" ]; then
            log "JDK version is not 11"
            ${JAVA} -version
            exit 0
    fi
}

checkMaven(){
    MVN="$MAVEN_HOME/bin/mvn"
    if [ ! -r "$MVN" ]; then
        MVN='mvn'
    fi
    MAVEN_EXIST=`${MVN} -version 2>&1 |grep "Apache Maven"`
    if [ ! -n "$MAVEN_EXIST" ]; then
            log "maven not install"
            exit 0
    fi
}

checkJDKAndMaven(){
    checkJavaVersion
    checkMaven
}

checkJDKAndMaven

#检查是否已经clone了nuls2.0
initAndUpdateNulsRuntime()
{

    if [ ! -d "$NULS2_REPO_DIR" ]; 
    then
        git clone $NULS2_REPO $NULS2_REPO_DIR
        cd $NULS2_REPO_DIR
        git checkout $NULS2_BRANCH
    else
        cd $NULS2_REPO_DIR
        git pull         
    fi
    if [ -x "mvn" ]; then
        if [ ! -d "./lib/maven" ]; then
            tar -xvf ./lib/apache-maven-3.6.1-bin.tar.gz
            mv ./apache-maven-3.6.1 ./maven
        fi
        path=`pwd`
        export MAVEN_HOME=${path}/lib/maven
        export ${PATH}:${MAVEN_HOME}/bin
    fi
    ./package -a "mykernel"
    ./package -N -o "../$1"
    cd ..
    # rm -rf $NULS2_REPO_DIR
}

getModuleTemplate(){
    templateName=$1
    dirName=$2
    if [ -z "$dirName" ]; then
        dirName="nuls-module-java"
    fi
    case "$templateName" in
        "java" )
            git clone $JAVA_MODULE_TEMP_REPO $dirName
           # git clone $NULS2_REPO $NULS2_REPO_DIR
           # cd $NULS2_REPO_DIR/common
           # mvn install -Dmaven.test.skip=true
           # cd ../..
            cd $dirName
            cat README.md
            exit 0;
            ;;
        ?)
            
            exit 0;
            ;;    
    esac
    echo "not found template : $templateName"
    echo "template list : ${TEMPLATLIST}"
}

packageToNuls(){
    module=$1
    if [ ! -d $module ]; then
        echo "wrong module folder"
        exit 0;
    fi
    cd $module
    if [ ! -d "outer" ]; then
        echo "not found outer folder"
        exit 0
    fi
    if [ ! -f "outer/Module.ncf" ]; then
        echo "not found Module.ncf"
        exit 0
    fi
    moduleName=`getModuleItem  "outer/Module.ncf" "APP_NAME"`;
    version=`getModuleItem  "outer/Module.ncf" "VERSION"`;
    if [ -z "$moduleName" ]; then
        echo "must setting APP_NAME"
        exit 0
    fi
    if [ -z "$version" ]; then
        echo "must setting VERSION"
        exit 0
    fi
    cd ..
    initAndUpdateNulsRuntime "./NULS-WALLET"
    if [ ! -d "./NULS-WALLET/Modules/Nuls/$moduleName" ]; then
        mkdir "./NULS-WALLET/Modules/Nuls/$moduleName"
    fi
    if [ -d "./NULS-WALLET/Modules/Nuls/$moduleName/$version" ]; then
        rm -rf "./NULS-WALLET/Modules/Nuls/$moduleName/$version"
    fi
    mkdir "./NULS-WALLET/Modules/Nuls/$moduleName/$version"
    cp -r "${module}/outer/" "./NULS-WALLET/Modules/Nuls/$moduleName/$version"
}


while getopts nt:p:h name
do
            case $name in
            p)     
                   packageToNuls "$OPTARG"
                   exit
                   ;;
            t)     
                   dir=`eval echo '$'"$OPTIND"`
                   getModuleTemplate "$OPTARG" "$dir"
                   exit 0
                   ;;
            n)     initAndUpdateNulsRuntime $NULS2_RUNTIME_DIR
                   exit 0
                   ;;
            h)     help
                   exit 0
                   ;;
            ?)     exit 2;;
           esac
done




