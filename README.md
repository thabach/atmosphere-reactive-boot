## Atmosphere Reactive
Super simple reactive microservice framework : No Spring, No Akka ... just Netty!

```java
@On("/dispatch")
public class MessageDispatcherService implements Service<Enveloppe> {
    private final Logger logger = LoggerFactory.getLogger(MessageDispatcherService.class);

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    private EventBus eventBus;
    
    @Inject
    private Mapper mapper;

    @Override
    public void on(Enveloppe enveloppe, Reply<Enveloppe> reply) throws IOException {
        Message message = mapper.readValue(enveloppe.body(), Message.class);

        eventBus.dispatch("/ChatService", message);
        
        reply.ok(enveloppe);
    }
}
```
