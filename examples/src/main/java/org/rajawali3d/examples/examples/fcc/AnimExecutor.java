package org.rajawali3d.examples.examples.fcc;

import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.AnimationGroup;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.ScaleAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class AnimExecutor {

    private List<Animation> anims = new ArrayList<Animation>();

    /**
     * 播放动画
     */
    public void playAnim() {
        Log.e("fcc", "playAnim: "+anims.size());
        for (Animation animation : anims) {
            animation.play();
        }
    }

    private void innerAddTransAnimToGroup(AnimationGroup animGroup, Object3D object3D,
                                          Vector3 vector3, boolean inverse, long duration) {
        innerAddTransAnimToGroup(animGroup, object3D, vector3, inverse, duration, null);
    }

    private void innerAddTransAnimToGroup(AnimationGroup animGroup, Object3D object3D,
                                          Vector3 vector3, boolean inverse, long duration, IAnimationListener listener) {
        TranslateAnimation3D transAnim = new TranslateAnimation3D(vector3);
        transAnim.setDurationMilliseconds(duration);
        transAnim.setTransformable3D(object3D);
        if (!inverse) {
            animGroup.setRepeatMode(Animation.RepeatMode.INFINITE);
        } else {
            animGroup.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        }
        transAnim.setInterpolator(new BounceInterpolator());
        if (listener != null) {
            transAnim.registerListener(listener);
        }
        animGroup.addAnimation(transAnim);
    }

    private void innerAddScaleAnimToGroup(AnimationGroup animGroup, Object3D object3D,
                                          float scale, boolean inverse, long duration) {
        ScaleAnimation3D scaleAnim = new ScaleAnimation3D(new Vector3(scale, scale, scale));
        scaleAnim.setDurationMilliseconds(duration);
        scaleAnim.setTransformable3D(object3D);
        if (!inverse) {
            animGroup.setRepeatMode(Animation.RepeatMode.INFINITE);
        } else {
            animGroup.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        }
        scaleAnim.setInterpolator(new LinearInterpolator());
        animGroup.addAnimation(scaleAnim);
    }

    private void innerAddRotateAnimToGroup(AnimationGroup animGroup, Object3D object3D,
                                    boolean inverse, long duration) {
        Vector3 axis = new Vector3(0, 0, 1);
        axis.normalize();
        RotateOnAxisAnimation rotateAnim = new RotateOnAxisAnimation(axis, 0, 360);
        rotateAnim.setDurationMilliseconds(duration);
        rotateAnim.setTransformable3D(object3D);
        animGroup.addAnimation(rotateAnim);
    }

    public void addTransScaleRotateAnim(Scene scene, Object3D object3D, Vector3 vector3, float scale, boolean inverse, IAnimationListener listener) {
        final AnimationGroup animGroup = new AnimationGroup();
        if (!inverse) {
            animGroup.setRepeatMode(Animation.RepeatMode.INFINITE);
        } else {
            animGroup.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        }
        if (listener != null) {
            animGroup.registerListener(listener);
        }

        long duration = 5000;

        innerAddTransAnimToGroup(animGroup, object3D, vector3, inverse, duration, listener);
        innerAddScaleAnimToGroup(animGroup, object3D, scale, inverse, duration);
        innerAddRotateAnimToGroup(animGroup, object3D, inverse, duration);

        anims.add(animGroup);
        scene.registerAnimation(animGroup);
    }

    public void addTransScaleAnim(Scene scene, Object3D object3D, Vector3 vector3, float scale, boolean inverse) {
        final AnimationGroup animGroup = new AnimationGroup();
        if (!inverse) {
            animGroup.setRepeatMode(Animation.RepeatMode.INFINITE);
        } else {
            animGroup.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        }
        animGroup.registerListener(new IAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationUpdate(Animation animation, double interpolatedTime) {

            }
        });

        long duration = 5000;

        innerAddTransAnimToGroup(animGroup, object3D, vector3, inverse, duration);
        innerAddScaleAnimToGroup(animGroup, object3D, scale, inverse, duration);

        anims.add(animGroup);
        scene.registerAnimation(animGroup);
    }

    public void addTransAnim(Scene scene, Object3D object3D, Vector3 vector3) {
        //位移动画
        TranslateAnimation3D anim = new TranslateAnimation3D(vector3);
        anim.setDurationMilliseconds(3000);
        anim.setTransformable3D(object3D);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.setInterpolator(new BounceInterpolator()); //到终点时候，会弹跳下
        anim.registerListener(new IAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e("fcc", "trans anim end");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
                Log.e("fcc", "trans anim start");
            }

            @Override
            public void onAnimationUpdate(Animation animation, double interpolatedTime) {

            }
        });
        anims.add(anim);
        scene.registerAnimation(anim);
    }

    public void addScaleAnim(Scene scene, Object3D object3D) {
        Animation3D anim = new ScaleAnimation3D(new Vector3(0.1f, 0.1f, 0.1f));
        anim.setInterpolator(new LinearInterpolator());
        anim.setDurationMilliseconds(3000);
        anim.setTransformable3D(object3D);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.registerListener(new IAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e("fcc", "scale anim end");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {
                Log.e("fcc", "scale anim start");
            }

            @Override
            public void onAnimationUpdate(Animation animation, double interpolatedTime) {

            }
        });
        anims.add(anim);
        scene.registerAnimation(anim);
    }

    public void clear() {
        anims.clear();
    }
}
