## websocket 记录

## 1.spring 整合websocket

### 1.不用注解的方式

#### 1.简单说明

```
1.spring整合websocket有两种方式
1.1 实现org.springframework.web.socket.WebSocketHandler
重写里面的方法
1. 方法 afterConnectionEstablished（），是连接之后进行的操作，类似于以前的 onopen 方法。 里面有一个参数 WebSocketSession，表示连接进来的那一个 Session. 可以通过 getAttributes() 方法，获取 HandshakeInterceptor 拦截器放置的 paramMap 集合。
2. 方法 handleMessage(), 是服务器接收浏览器发送过来的消息进行的操作，类似于以前的 onmessage 方法。 WebSocketSession 对象表示 发送消息的那一个Session, WebSocketMessage 表示发送的消息主体。
3. 方法 handleTransportError（）是出现异常时进行的操作，类似于以前的 onerror 方法。 WebSocketSession 对象表示哪一个Session 出现了错误异常。
4. 方法 afterConnectionClosed（）,是浏览器断开连接或者服务器断开连接的操作，类似于以前的 onclose 方法，WebSocketSession 表示 断开的是哪一个Session
5. 方法 supportsPartialMessages（） 表示是否支持拆分。 当浏览器输入的内容过多时，允不允许将接收到的内容，进行拆分处理。通常不允许拆分, 返回 false 即可。

1.2 利用注解注释类方法,这种是用java自带的包
javax.websocket.ClientEndpoint
javax.websocket.ServerEndpoint
javax.websocket.Session

@OnOpen
@OnMessage
@OnError
@OnClose

public void send(String message) {
        this.session.getAsyncRemote().sendText(message);
    }
```

#### 2.服务端

##### 1.pom.xml

```
<dependencies>
        <!---->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-websocket</artifactId>
            <version>4.2.4.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>xml
    
```

##### 2.拦截器

```java
1.继承spring提供的拦截器接口org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

2.内容
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest request,
								   ServerHttpResponse response, WebSocketHandler wsHandler,
								   Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
			// 获取参数
			String userId = serverHttpRequest.getServletRequest().getParameter(
					"userId");
			attributes.put("currentUser", userId);
		}

		return true;
	}

	// 初次握手访问后
	@Override
	public void afterHandshake(ServerHttpRequest serverHttpRequest,
			ServerHttpResponse serverHttpResponse,
			WebSocketHandler webSocketHandler, Exception e) {

	}
3.方法说明	
beforeHandshake() 方法，是请求连接之前的处理方法,
afterHandshake() 方法，是请求连接成功之后的处理方法。
```

