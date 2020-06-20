package com.example.projetopi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * #DevicesFragment
 *
 * In this fragment, the bluetooth devices are displayed and the user can choose one of them
 * to connect. After the connection is succeeded, the user can send the required information after
 * typing all the data and hitting the Send button.
 *
 * It also is displayed the messages received from the bluetooth module.
 *
 * It shows different message errors depending on the problem at hand.
 */
public class DevicesFragment extends ListFragment {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;

    /**
     * ##onCreate
     *
     * This function is executed every time this instance is ran.
     *
     * It initializes the bluetooth adapter if the mobile phone supports it and searches for
     * available paring devices nearby.
     *
     * @param savedInstanceState Internal variable used to save the instance activity state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            /**
             * ##getView function
             *
             * This function sets up the interface views on this instance
             *
             * @param position The position of the bluetooth module that is shown in the list
             * @param view The view used in this instance [device_list_item]
             * @param parent The parent refers to DevicesFragment instance
             * @return The view is returned to be used in the parent
             */
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
                text1.setText(device.getName());
                text2.setText(device.getAddress());
                return view;
            }
        };
    }

    /**
     * ##onActivityCreated
     *
     * As the name suggest, this function is ran every time the activity is created. It functions as
     * a initial setup of the activity..
     *
     * @param savedInstanceState Internal variable used to save the instance activity state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        View header = getActivity().getLayoutInflater().inflate(R.layout.device_list_header, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("initializing...");
        ((TextView) getListView().getEmptyView()).setTextSize(18);
        setListAdapter(listAdapter);
    }

    /**
     * ##onCreateOptionsMenu
     *
     * This function creates the options menu when the activity is created
     *
     * @param menu Defines the menu, which is a container for the items of the menu
     * @param inflater This is used to inflate the resources of the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        if(bluetoothAdapter == null)
            menu.findItem(R.id.bt_settings).setEnabled(false);
    }

    /**
     * ##onResume
     *
     * This function is executed when the used resumes the app, either from unlocking the mobile phone
     * or either from resuming the activity from background
     */
    @Override
    public void onResume() {
        super.onResume();
        if(bluetoothAdapter == null)
            setEmptyText("<bluetooth not supported>");
        else if(!bluetoothAdapter.isEnabled())
            setEmptyText("<bluetooth is disabled>");
        else
            setEmptyText("<no bluetooth devices found>");
        refresh();
    }

    /**
     * ##onOptionsItemSelected
     *
     * This function is executed every time the user selects an option in the options menu
     *
     * @param item Item selected in the options menu in the form of a button
     * @return Returns true if the action was succeeded
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bt_settings) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ##refresh
     *
     * Simple refresh of the listItems of bluetooth modules nearby
     */
    void refresh() {
        listItems.clear();
        if(bluetoothAdapter != null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices())
                if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE)
                    listItems.add(device);
        }
        Collections.sort(listItems, DevicesFragment::compareTo);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * ##onListItemClick
     *
     * As the name suggest, this function is executed any time the user selects a item in the item list,
     * in this case, a bluetooth module.
     *
     * @param l ListView where the item belongs
     * @param v View where the listview belongs
     * @param position Position of the item clicked
     * @param id ID of the item clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        BluetoothDevice device = listItems.get(position-1);
        Bundle args = new Bundle();
        args.putString("device", device.getAddress());
        Fragment fragment = new TerminalFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
    }

    /**
     * ## compareTo
     *
     * This function sorts the listItem by name, then address. It sorts named devices first.
     */
    static int compareTo(BluetoothDevice a, BluetoothDevice b) {
        boolean aValid = a.getName()!=null && !a.getName().isEmpty();
        boolean bValid = b.getName()!=null && !b.getName().isEmpty();
        if(aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if(aValid) return -1;
        if(bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }
}
