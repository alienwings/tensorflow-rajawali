/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.util;

import android.graphics.PointF;
import java.util.ArrayList;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.visitors.RayPickingVisitor;

public class RayPicker implements IObjectPicker {
	private Renderer               mRenderer;
	private OnObjectPickedListener mObjectPickedListener;

	public RayPicker(Renderer renderer) {
		mRenderer = renderer;
	}

	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}

	public void getObjectAt(float screenX, float screenY) {
		getObjectAt(mRenderer.screenToCartesian(screenX,screenY));
	}

	public void getObjectAt(PointF position) {
		Vector3 pointNear = mRenderer.unProject(position.x, position.y, 0);
		Vector3 pointFar = mRenderer.unProject(position.x, position.y, 1);

		RayPickingVisitor visitor = new RayPickingVisitor(pointNear, pointFar);
	        ArrayList<Object3D> objects = mRenderer.getCurrentScene().getChildrenCopy();
		// TODO: ray-triangle intersection test
	        for(Object3D object : objects) {
	            object.accept(visitor);
	        }

		if(visitor.getPickedObject() == null) {
                    mObjectPickedListener.onNoObjectPicked();
                } else {
                    mObjectPickedListener.onObjectPicked(visitor.getPickedObject());
                }
	}
}
