/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm;

import static android.window.DisplayAreaOrganizer.FEATURE_DEFAULT_TASK_CONTAINER;

import android.content.res.Resources;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Policy that manages DisplayAreas.
 */
public abstract class DisplayAreaPolicy {
    protected final com.android.server.wm.WindowManagerService mWmService;
    protected final com.android.server.wm.DisplayContent mContent;

    /**
     * The root DisplayArea. Attach all DisplayAreas to this area (directly or indirectly).
     */
    protected final com.android.server.wm.DisplayArea.Root mRoot;

    /**
     * The IME container. The IME's windows are automatically added to this container.
     */
    protected final com.android.server.wm.DisplayArea<? extends com.android.server.wm.WindowContainer> mImeContainer;

    /**
     * The task display areas. Tasks etc. are automatically added to these containers.
     */
    protected final List<com.android.server.wm.TaskDisplayArea> mTaskDisplayAreas;

    /**
     * Construct a new {@link DisplayAreaPolicy}
     *
     * @param wmService the window manager service instance
     * @param content the display content for which the policy applies
     * @param root the root display area under which the policy operates
     * @param imeContainer the ime container that the policy must attach
     * @param taskDisplayAreas the task display areas that the policy must attach
     *
     * @see #attachDisplayAreas()
     */
    protected DisplayAreaPolicy(com.android.server.wm.WindowManagerService wmService,
                                com.android.server.wm.DisplayContent content, com.android.server.wm.DisplayArea.Root root,
                                com.android.server.wm.DisplayArea<? extends com.android.server.wm.WindowContainer> imeContainer,
                                List<com.android.server.wm.TaskDisplayArea> taskDisplayAreas) {
        mWmService = wmService;
        mContent = content;
        mRoot = root;
        mImeContainer = imeContainer;
        mTaskDisplayAreas = taskDisplayAreas;
    }

    /**
     * Called to ask the policy to set up the DisplayArea hierarchy. At a minimum this must:
     *
     * - attach mImeContainer to mRoot (or one of its descendants)
     * - attach mTaskStacks to mRoot (or one of its descendants)
     *
     * Additionally, this is the right place to set up any other DisplayAreas as desired.
     */
    public abstract void attachDisplayAreas();

    /**
     * Called to ask the policy to attach the given WindowToken to the DisplayArea hierarchy.
     *
     * This must attach the token to mRoot (or one of its descendants).
     */
    public abstract void addWindow(com.android.server.wm.WindowToken token);

    /**
     * @return the number of task display areas on the display.
     */
    public int getTaskDisplayAreaCount() {
        return mTaskDisplayAreas.size();
    }

    /**
     * @return the task display area at index.
     */
    public com.android.server.wm.TaskDisplayArea getTaskDisplayAreaAt(int index) {
        return mTaskDisplayAreas.get(index);
    }

    /** Provider for platform-default display area policy. */
    static final class DefaultProvider implements DisplayAreaPolicy.Provider {
        @Override
        public DisplayAreaPolicy instantiate(com.android.server.wm.WindowManagerService wmService,
                                             com.android.server.wm.DisplayContent content, com.android.server.wm.DisplayArea.Root root,
                                             com.android.server.wm.DisplayArea<? extends com.android.server.wm.WindowContainer> imeContainer) {
            // 创建一个默认的显示区域
            final com.android.server.wm.TaskDisplayArea defaultTaskDisplayArea = new com.android.server.wm.TaskDisplayArea(content, wmService,
                    "DefaultTaskDisplayArea", FEATURE_DEFAULT_TASK_CONTAINER);
            final List<com.android.server.wm.TaskDisplayArea> tdaList = new ArrayList<>();
            tdaList.add(defaultTaskDisplayArea);
            return new com.android.server.wm.DisplayAreaPolicyBuilder()
                    .build(wmService, content, root, imeContainer, tdaList);
        }
    }

    /**
     * Provider for {@link DisplayAreaPolicy} instances.
     *
     * By implementing this interface and overriding the
     * {@code config_deviceSpecificDisplayAreaPolicyProvider}, a device-specific implementations
     * of {@link DisplayAreaPolicy} can be supplied.
     */
    public interface Provider {
        /**
         * Instantiate a new DisplayAreaPolicy.
         *
         * @see DisplayAreaPolicy#DisplayAreaPolicy
         */
        DisplayAreaPolicy instantiate(com.android.server.wm.WindowManagerService wmService,
                                      com.android.server.wm.DisplayContent content, com.android.server.wm.DisplayArea.Root root,
                                      com.android.server.wm.DisplayArea<? extends com.android.server.wm.WindowContainer> imeContainer);

        /**
         * Instantiate the device-specific {@link Provider}.
         * 提供给特别的设备使用，如多屏幕。
         */
        static Provider fromResources(Resources res) {
            String name = res.getString(
                    com.android.internal.R.string.config_deviceSpecificDisplayAreaPolicyProvider);
            if (TextUtils.isEmpty(name)) {
                // 没有就使用默认的
                return new DisplayAreaPolicy.DefaultProvider();
            }
            try {
                return (Provider) Class.forName(name).newInstance();
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new IllegalStateException("Couldn't instantiate class " + name
                        + " for config_deviceSpecificDisplayAreaPolicyProvider:"
                        + " make sure it has a public zero-argument constructor"
                        + " and implements DisplayAreaPolicy.Provider", e);
            }
        }
    }
}
