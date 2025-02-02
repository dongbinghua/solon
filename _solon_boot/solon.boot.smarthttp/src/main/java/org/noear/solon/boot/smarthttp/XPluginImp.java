package org.noear.solon.boot.smarthttp;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.prop.impl.HttpServerProps;
import org.noear.solon.boot.smarthttp.http.SmartHttpContextHandler;
import org.noear.solon.boot.smarthttp.http.FormContentFilter;
import org.noear.solon.boot.smarthttp.websocket.WebSocketHandleImp;
import org.noear.solon.boot.smarthttp.websocket._SessionManagerImpl;
import org.noear.solon.boot.ssl.SslContextFactory;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.socketd.SessionManager;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.extension.plugins.SslPlugin;

import javax.net.ssl.SSLContext;

public final class XPluginImp implements Plugin {
    private static Signal _signal;

    public static Signal signal() {
        return _signal;
    }

    HttpBootstrap _server = null;

    public static String solon_boot_ver() {
        return "smart http 1.1/" + Solon.version();
    }

    @Override
    public void start(AopContext context) {
        if (Solon.app().enableHttp() == false) {
            return;
        }

        context.lifecycle(-99, () -> {
                start0(Solon.app());
        });
    }

    private void start0(SolonApp app) throws Throwable {
        //初始化属性
        ServerProps.init();

        HttpServerProps props = new HttpServerProps();
        final String _host = props.getHost();
        final int _port = props.getPort();
        final String _name = props.getName();

        long time_start = System.currentTimeMillis();

        SmartHttpContextHandler _handler = new SmartHttpContextHandler();

        if(props.isIoBound()) {
            //如果是io密集型的，加二段线程池
            _handler.setExecutor(props.getBioExecutor("smarthttp-"));
        }

        _server = new HttpBootstrap();
        HttpServerConfiguration _config = _server.configuration();
        if (Utils.isNotEmpty(_host)) {
            _config.host(_host);
        }

        if (System.getProperty(ServerConstants.SSL_KEYSTORE) != null) {
            SSLContext sslContext = SslContextFactory.create();

            SslPlugin<Request> sslPlugin = new SslPlugin<>(() -> sslContext, sslEngine -> {
                sslEngine.setUseClientMode(false);
            });
            _config.addPlugin(sslPlugin);
        }

        //_config.debug(Solon.cfg().isDebugMode());

        _config.bannerEnabled(false);
        _config.readBufferSize(1024 * 8); //默认: 8k
        _config.threadNum(props.getCoreThreads());


        if (ServerProps.request_maxHeaderSize != 0) {
            _config.readBufferSize(ServerProps.request_maxHeaderSize);
        }

        if (ServerProps.request_maxBodySize != 0) {
            _config.setMaxFormContentSize(ServerProps.request_maxBodySize);
        }


        //HttpServerConfiguration
        EventBus.push(_config);

        _server.httpHandler(_handler);

        if (app.enableWebSocket()) {
            _server.webSocketHandler(new WebSocketHandleImp());

            SessionManager.register(new _SessionManagerImpl());
        }

        LogUtil.global().info("Server:main: SmartHttpServer 1.1(smarthttp)");


        _server.setPort(_port);
        _server.start();

        final String _wrapHost = props.getWrapHost();
        final int _wrapPort = props.getWrapPort();
        _signal = new SignalSim(_name, _wrapHost, _wrapPort,"http", SignalType.HTTP);

        app.signalAdd(_signal);

        app.before(-9, new FormContentFilter());

        long time_end = System.currentTimeMillis();

        String connectorInfo = "solon.connector:main: smarthttp: Started ServerConnector@{HTTP/1.1,[http/1.1]";
        if (app.enableWebSocket()) {
            LogUtil.global().info(connectorInfo + "[WebSocket]}{0.0.0.0:" + _port + "}");
        }

        LogUtil.global().info(connectorInfo + "}{http://localhost:" + _port + "}");

        LogUtil.global().info("Server:main: smarthttp: Started @" + (time_end - time_start) + "ms");
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.shutdown();
            _server = null;

            LogUtil.global().info("Server:main: smarthttp: Has Stopped " + solon_boot_ver());
        }
    }
}

