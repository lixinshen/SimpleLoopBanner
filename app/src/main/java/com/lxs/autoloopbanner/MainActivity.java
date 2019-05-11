package com.lxs.autoloopbanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lxs.simpleloopbanner.SimpleLoopBanner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SimpleLoopBanner simpleLoopBanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleLoopBanner = findViewById(R.id.simple_loop_banner);

        setSimpleLoopBanner();

    }

    private void setSimpleLoopBanner() {
        List<String> images = new ArrayList<>();
        images.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1524714908870&di=9d43d35cefbabacdc879733aa7ddc82b&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimage%2Fc0%253Dshijue1%252C0%252C0%252C294%252C40%2Fsign%3D46de93bfc711728b24208461a095a9bb%2F4610b912c8fcc3ce5423d51d9845d688d43f2038.jpg");
        images.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1524714935901&di=052557513540f3d740eeeb2439c585bb&imgtype=0&src=http%3A%2F%2Fwww.gzlco.com%2Fimggzl%2F214%2F1b6e6520ca474fe4bd3ff728817950717651.jpeg");

        simpleLoopBanner.setImagesUrl(images);

    }
}
