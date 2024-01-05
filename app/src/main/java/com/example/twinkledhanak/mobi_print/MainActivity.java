package com.example.twinkledhanak.mobi_print;

import android.content.Context;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;

public class MainActivity extends AppCompatActivity {

    public static final String SERVICE_TYPE= "_ipp._tcp";

    NsdManager.RegistrationListener mRegistrationListener;
    NsdServiceInfo serviceInfo;
    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    String mServiceName;
    Connection mConnection;
    NsdManager mNsdHelper;
    NsdManager.ResolveListener mResolveListener;
    Button button;
    NsdServiceInfo mService;
    ServerSocket mServerSocket;
    int mLocalPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                try {
                    startcalls();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void startcalls() throws IOException {

        mServerSocket= new ServerSocket(0);
        mLocalPort= mServerSocket.getLocalPort();
      // registerService(10180);
        registerService(mLocalPort);

    }


    // BELOW METHOD REGISTERSERVICE() IS DIFFERENT FROM REGISTERSERVICE( , , )
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void registerService(int port) {

        serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("printing");
        serviceInfo.setServiceType("_ipp._tcp");// specifying that service is of type printing service
        serviceInfo.setPort(port);

        // mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

// registrationListener is to be initialized before use.. so instead of different functions, we wwrite the function here
// so after registration listener is defined, we call the register service method
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();

                Toast.makeText(MainActivity.this, " our service registration done successfully", Toast.LENGTH_SHORT).show();
                // after getting the service, we again refresh its name and write code for further execution

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Toast.makeText(MainActivity.this,"service registration failed", Toast.LENGTH_SHORT).show();
            }
            // so far below two methods are not required

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode){

                // Unregistration failed.  Put debugging code here to determine why.
            }
        };
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

// even for discoverServices() , we needed discoveryListener as parameter, so we initialized it first and then called it
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {

                Toast.makeText(MainActivity.this,"service discovery started",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.

                Toast.makeText(MainActivity.this,"service discovery success",Toast.LENGTH_SHORT).show();
                Log.d("service name found",service.getServiceName());
                Log.d("service type found",service.getServiceType());

                // for network printing we only need to check if the service type is _ipp._tcp

                if (!service.getServiceType().equals("_ipp._tcp")) // SERVICE_TYPE is ipp for network printing
                {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.

                    Toast.makeText(MainActivity.this," Unknown service type",Toast.LENGTH_SHORT).show();


                }

                else if (service.getServiceType().equals("_ipp._tcp"))
                {
                    // here, we will display list of all services discovered by NSD and then display them in
                    // list view, when user clicks on any of these service, we will call method resolveService()

                    mNsdManager.resolveService(service, mResolveListener);
                }
                else if(service.getServiceName().contains("printing"))
                {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.

                Toast.makeText(MainActivity.this," service lost",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {

                Toast.makeText(MainActivity.this," Discovery stopped",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {

                Toast.makeText(MainActivity.this," discovery failed on start ",Toast.LENGTH_SHORT).show();
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {

                Toast.makeText(MainActivity.this," discovery failed on stop ",Toast.LENGTH_SHORT).show();
                mNsdManager.stopServiceDiscovery(this);
            }
        };

        ///-------
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.

                Toast.makeText(MainActivity.this,"resolve failed ",Toast.LENGTH_SHORT).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                Toast.makeText(MainActivity.this," Resolve Succeeded ",Toast.LENGTH_SHORT).show();

                if (serviceInfo.getServiceType().equals(SERVICE_TYPE)) {

                    return;
                }
                mService = serviceInfo;
                int port = mService.getPort();  // these both are detailed service information about device
                InetAddress host = mService.getHost();
                Toast.makeText(MainActivity.this,"port"+port+" ",Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this,"host"+host+" ",Toast.LENGTH_LONG).show();

                // both port no and host address is obtained
            }
        };

        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }








    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            //mNsdHelper.tearDown();
        }
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
           // mNsdHelper.registerService(mConnection.getLocalPort());
            mNsdHelper.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }
    }

    @Override
    protected void onDestroy() {
       // mNsdHelper.tearDown();
       // mConnection.tearDown();
        super.onDestroy();
    }

    // NsdHelper's tearDown method
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }





}
