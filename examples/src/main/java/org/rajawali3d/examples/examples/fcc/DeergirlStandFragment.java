package org.rajawali3d.examples.examples.fcc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.rajawali3d.examples.MyApp;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.loader.AMeshLoader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.renderer.ISurfaceRenderer;

public class DeergirlStandFragment extends AExampleFragment {

    ProgressBar progressBar;
    TextView hint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.activity_launcher, container, false);
        progressBar = root.findViewById(R.id.progress_bar);
        hint = root.findViewById(R.id.hint);
        new LoadModelTask().execute();
        return root;
    }

    @Override
    public ISurfaceRenderer createRenderer() {
        return null;
    }

    public class LoadModelTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            hint.setVisibility(View.VISIBLE);
            hint.setText("u a loading model....");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
/*            if (false) {
                try {
                    DeerGirlARCoreRenderer coreRender = new SkeletalMD5Demo2Render(MyApp.getContext());

                    DeerGirlActivity.setSRender(coreRender);
                    AMeshLoader meshParser = null;
                    if (coreRender instanceof SkeletalMD5Demo1Render) {
                        meshParser = new LoaderMD5Mesh(coreRender, R.raw.boblampclean_mesh);
                    }
                    else if (coreRender instanceof SkeletalMD5Demo2Render) {
                        meshParser = new LoaderMD5Mesh(coreRender, R.raw.deergirl_ch_cloth_mesh);
                    }
                    coreRender.setSMeshParser(meshParser);
//                meshParser.parse();
                } catch (Exception e) {
                    e.printStackTrace();
                    hint.setText("Error: " + e);
                }
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.INVISIBLE);
            hint.setText("Finish loading model.");
            Intent intent;
//        intent = new Intent(getActivity(), HelloArActivity.class);
            intent = new Intent(getActivity(), DeerGirlActivity.class);
            startActivity(intent);
        }
    }
}
