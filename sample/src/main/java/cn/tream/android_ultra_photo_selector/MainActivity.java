package cn.tream.android_ultra_photo_selector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;

import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoPreviewActivity;
import com.photoselector.ui.PhotoSelectorActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private EditText et_size;
    private CheckBox cb_showCamera;
    private GridView gridView;
    private static final int REQUEST_SELECT = 0;
    private static final int REQUEST_PREVIEW = 1;
    private PhotoGridViewAdapter photoGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_size = (EditText) findViewById(R.id.size);
        cb_showCamera = (CheckBox) findViewById(R.id.showCamera);
        findViewById(R.id.ok).setOnClickListener(this);
        gridView = (GridView) findViewById(R.id.gridView);
        photoGridViewAdapter = new PhotoGridViewAdapter(this);
        gridView.setAdapter(photoGridViewAdapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoGridViewAdapter.setItemWidth(gridView.getNumColumns(), gridView.getMeasuredWidth());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ok) {
            Intent intent = new Intent(this, PhotoSelectorActivity.class);
            String text = et_size.getText().toString().trim();
            int maxSize = TextUtils.isEmpty(text) ? 9 : Integer.parseInt(text);
            intent.putExtras(PhotoSelectorActivity.getBundle(maxSize > 0 ? maxSize : 9, cb_showCamera.isChecked()));
            startActivityForResult(intent, REQUEST_SELECT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case REQUEST_SELECT:
                photoGridViewAdapter.setItemList((ArrayList<PhotoModel>) data.getSerializableExtra("photos"));
                break;
            case REQUEST_PREVIEW:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        photoGridViewAdapter.getItem(position);
        Bundle bundle = PhotoPreviewActivity.getBundle(photoGridViewAdapter.getItemList(), position);
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }
}
