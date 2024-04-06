package com.yupi.springbootinit.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.auth.STSAssumeRoleSessionCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;

/**
 * @author mikufans
 * @version 1.0.0
 * @title 配置oss访问凭证
 * @description <miku的自定义模板>
 */
public class OssConfig {
    // 授权STSAssumeRole访问的Region。
    String region = "cn-shanghai";
    // 从环境变量中获取RAM用户的访问密钥（AccessKey ID和AccessKey Secret）。
    String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
    String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
    // 从环境变量中获取RAM角色的RamRoleArn。
    String roleArn = System.getenv("OSS_STS_ROLE_ARN");

    // 使用环境变量中获取的RAM用户的访问密钥和RAM角色的RamRoleArn配置访问凭证。
    STSAssumeRoleSessionCredentialsProvider credentialsProvider = CredentialsProviderFactory
            .newSTSAssumeRoleSessionCredentialsProvider(
                    region,
                    accessKeyId,
                    accessKeySecret,
                    roleArn
            );

    public OssConfig() throws ClientException {
    }


}
