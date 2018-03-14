import akka.actor.ActorRef;
import akka.actor.Props;
import play.GlobalSettings;
import play.libs.Akka;

public class Global extends GlobalSettings {

    @Override
    public void onStart(play.Application application) {
        super.onStart(application);
    }

}