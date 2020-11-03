package demo;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        export();
    }

    public static void export(){
// 服务实现
        HelloService xxxService = new HelloServiceImpl();

// 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("xxx");

// 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setProtocol("nacos");
        registry.setAddress("nacos.didispace.com:80");
//        registry.setUsername("aaa");
//        registry.setPassword("bbb");

// 服务提供者协议配置
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(12345);
        protocol.setThreads(10);

// 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口

// 服务提供者暴露服务配置
        ServiceConfig<HelloService> service = new ServiceConfig<HelloService>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
        service.setApplication(application);
        service.setRegistry(registry); // 多个注册中心可以用setRegistries()
        service.setProtocol(protocol); // 多个协议可以用setProtocols()
        service.setInterface(HelloService.class);
        service.setRef(xxxService);
        service.setVersion("1.0.0");

// 暴露及注册服务
        service.export();
    }

}
