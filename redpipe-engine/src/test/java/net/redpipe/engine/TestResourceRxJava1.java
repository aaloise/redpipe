package net.redpipe.engine;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.Stream;
import org.jboss.resteasy.annotations.Stream.MODE;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.auth.AuthProvider;
import io.vertx.rxjava.ext.auth.User;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.Session;
import net.redpipe.engine.core.AppGlobals;
import net.redpipe.engine.security.HasPermission;
import net.redpipe.engine.security.NoAuthRedirect;
import net.redpipe.engine.security.RequiresPermissions;
import net.redpipe.engine.security.RequiresUser;
import rx.Completable;
import rx.Observable;
import rx.Single;


@Path("/rx1")
public class TestResourceRxJava1 {

	@Path("inject")
	@GET
	public String inject(@Context Vertx vertx,
			@Context RoutingContext routingContext,
			@Context HttpServerRequest request,
			@Context HttpServerResponse response,
			@Context AuthProvider authProvider,
			@Context User user,
			@Context Session session) {
		if(vertx == null
				|| routingContext == null
				|| request == null
				|| response == null
				|| session == null)
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		return "ok";
	}

	@Path("hello-single")
	@GET
	public Single<String> helloSingle() {
		return Single.just("hello");
	}

	@Stream(MODE.RAW)
	@Path("hello-observable")
	@GET
	public Observable<String> helloObservable() {
		return Observable.just("one", "two");
	}

	@Produces(MediaType.APPLICATION_JSON)
	@Path("hello-observable-collect")
	@GET
	public Observable<String> helloObservableCollect() {
		return Observable.just("one", "two");
	}

	
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@Path("hello-observable-sse")
	@GET
	public Observable<String> helloObservableSse() {
		return Observable.just("one", "two");
	}

	@NoAuthRedirect
	@RequiresUser
	@Path("inject-user")
	@GET
	public String injectUser(@Context User user,
			@Context AuthProvider authProvider) {
		if(user == null
				|| authProvider == null)
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		return "ok";
	}

	@NoAuthRedirect
	@RequiresPermissions("create")
	@Path("auth-create")
	@GET
	public Single<String> authCreate() {
		return Single.just("ok").delay(1, TimeUnit.SECONDS);
	}

	@NoAuthRedirect
	@RequiresUser
	@Path("auth-check")
	@GET
	public Single<Boolean> authCheck(@Context User user,
			@Context @HasPermission("create") boolean second) {
		return user.rxIsAuthorised("create").map(first -> first && second);
	}

	@Path("context-single")
	@GET
	public Single<String> contextPropagation(@Context UriInfo uriInfo,
			@Context AppGlobals globals){
		return Single.just("ok")
				.delay(1, TimeUnit.SECONDS)
				.map(string -> {
					System.err.println("uri: "+uriInfo.getPath());
					if(globals != AppGlobals.get())
						return "invalid-globals";
					return string;
				});
	}

	@Stream(MODE.RAW)
	@Path("context-observable")
	@GET
	public Observable<String> contextPropagationObservable(@Context UriInfo uriInfo,
			@Context AppGlobals globals){
		return Observable.just("o", "k")
				.delay(1, TimeUnit.SECONDS)
				.map(string -> {
					System.err.println("uri: "+uriInfo.getPath());
					if(globals != AppGlobals.get())
						return "invalid-globals";
					return string;
				});
	}

    @GET
    @Path("completable")
    public Completable returnCompletable() {
        return Completable.complete(); // should be 204
    }
}
