package visahall.cn.didida;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yancy.imageselector.ImageConfig;
import com.yancy.imageselector.ImageSelector;
import com.yancy.imageselector.ImageSelectorActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 * Created by Yancy on 2015/12/4.
 */
public class UpLoadImageActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private Button btn;
    private Button btn_submit;


    private Adapter adapter;

    private ArrayList<String> path = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadimage);

        btn = (Button) findViewById(R.id.btn);
        btn_submit = (Button) super.findViewById(R.id.btn_submit);
        recycler = (RecyclerView) super.findViewById(R.id.recycler);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageConfig imageConfig = new ImageConfig.Builder(UpLoadImageActivity.this, new GlideLoader())
                        .steepToolBarColor(getResources().getColor(R.color.blue))
                        .titleBgColor(getResources().getColor(R.color.blue))
                        .titleSubmitTextColor(getResources().getColor(R.color.white))
                        .titleTextColor(getResources().getColor(R.color.white))
                        .mutiSelect()
                        .mutiSelectMaxSize(9)
                        .pathList(path)
                        .filePath("/ImageSelector/Pictures")
                        .showCamera()
                        .build();


                ImageSelector.open(imageConfig);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path.size() == 0){
                    Toast.makeText(UpLoadImageActivity.this, "请先选择需要上传的图片", Toast.LENGTH_SHORT).show();
                }else {
                    for (int i = 0; i < path.size(); i++) {
                        Log.e("vvvvvvvv", path.get(i));
                    }
                }
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(gridLayoutManager);
        adapter = new Adapter(this, path);
        recycler.setAdapter(adapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("ccccccccc", path.size() + "");

        if (path.size() == 0){
            adapter = new Adapter(this, path);
            recycler.setAdapter(adapter);
        }else if (resultCode == RESULT_OK ){
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);

            for (String path : pathList) {
                Log.e("ImagePathList", path);
            }

            path.clear();
            path.addAll(pathList);
            adapter.notifyDataSetChanged();
        }

    }
}

