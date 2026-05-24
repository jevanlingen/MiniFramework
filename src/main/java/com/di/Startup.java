import com.di.architecture.ApplicationContext;
import com.di.architecture.Server;

void main() {
    final var appContext = new ApplicationContext();
    appContext.setup();

    appContext.getBean(Server.class).run();
}

