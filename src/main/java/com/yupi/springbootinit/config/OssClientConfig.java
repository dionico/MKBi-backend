package com.yupi.springbootinit.config;



import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyuncs.exceptions.ClientException;


/**
 * @author mikufans
 * @version 1.0.0
 * @title OssClientConfig
 * @description <miku的自定义模板>
 */
public class OssClientConfig {
    // yourEndpoint填写Bucket所在地域对应的Endpoint。
    String endpoint = "https://oss-cn-shanghai.aliyuncs.com";

    // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。

    // 从环境变量中获取访问凭证。运行本代码示例之前，请先配置环境变量。
    EnvironmentVariableCredentialsProvider credentialsProvider;

    {
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建OSSClient实例。
    OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

// 关闭OSSClient。
//ossClient.shutdown();

    // 填写Endpoint对应的Region信息，例如cn-hangzhou。
//    String region = "cn-shanghai";
//
//    // 从环境变量中获取访问凭证。运行本代码示例之前，请先配置环境变量。
//    EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
//
//    // 创建OSSClient实例。
//    ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
//    clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
//    OSS ossClient = OSSClientBuilder.creat()
//            .endpoint(endpoint)
//            .credentialsProvider(credentialsProvider)
//            .clientConfiguration(clientBuilderConfiguration)
//            .region(region)
//            .build()
//
//    public OssClientConfig() throws ClientException {
//    }
//
//    // 关闭OSSClient。
//    ossClient.shutdown();

}
