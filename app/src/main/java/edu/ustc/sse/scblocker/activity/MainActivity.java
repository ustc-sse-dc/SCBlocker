package edu.ustc.sse.scblocker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.fragment.BlockContentFragment;
import edu.ustc.sse.scblocker.fragment.RuleFragment;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private Fragment currentFragment;


    private FloatingActionButton fab = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragment instanceof RuleFragment){
                    Intent intent = RuleEditActivity.newIntent(MainActivity.this, RuleEditActivity.OPERATION_ADD);
                    currentFragment.startActivityForResult(intent, RuleFragment.REQUEST_CODE);
                }
            }
        });
        fab.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentFragment = BlockContentFragment.newInstance(BlockManager.TYPE_ALL);
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container_relativelayout, currentFragment)
                .commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_rule) {
            //TODO: Open add rule activity
            if (currentFragment instanceof RuleFragment){
                Intent intent = RuleEditActivity.newIntent(this, RuleEditActivity.OPERATION_ADD);
                currentFragment.startActivityForResult(intent, RuleFragment.REQUEST_CODE);
            }else {
                Snackbar.make(fab, "Add rule only allowed in rule's panel!", Snackbar.LENGTH_LONG).show();
            }
        } else if (id == R.id.all_rules) {
            currentFragment = new RuleFragment();
            switchFragment(currentFragment);
            fab.show();
        } else if (id == R.id.blockcontent_all) {
            currentFragment = BlockContentFragment.newInstance(BlockContent.BLOCK_ALL);
            switchFragment(currentFragment);
            fab.hide();
        } else if (id == R.id.blockcontent_call) {
            currentFragment = BlockContentFragment.newInstance(BlockContent.BLOCK_CALL);
            switchFragment(currentFragment);
            fab.hide();
        } else if (id == R.id.blockcontent_sms) {
            currentFragment = BlockContentFragment.newInstance(BlockContent.BLOCK_SMS);
            switchFragment(currentFragment);
            fab.hide();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void switchFragment(Fragment fragment){
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_relativelayout, fragment)
                .commit();
    }

}
