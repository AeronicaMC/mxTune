/*
 * MIT License
 *
 * Copyright (c) 2017 Hindol Adhya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * https://github.com/Hindol/commons
 */

package aeronicamc.mods.mxtune.caches;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.*;

@SuppressWarnings("unused")
public class DirectoryWatcher implements Runnable, Service {

    private static final Logger LOGGER = LogManager.getLogger(DirectoryWatcher.class);

    public enum Event {
        ENTRY_CREATE,
        ENTRY_MODIFY,
        ENTRY_DELETE
    }

    private static final Map<WatchEvent.Kind<Path>, Event> EVENT_MAP;
    static
    {
        EVENT_MAP = new HashMap<>();
        EVENT_MAP.put(ENTRY_CREATE, Event.ENTRY_CREATE);
        EVENT_MAP.put(ENTRY_MODIFY, Event.ENTRY_MODIFY);
        EVENT_MAP.put(ENTRY_DELETE, Event.ENTRY_DELETE);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private Future<?> mWatcherTask;

    private final Set<Path> mWatched;
    private final boolean mPreExistingAsCreated;
    private final Listener mListener;
    private final Filter<Path> mFilter;

    public DirectoryWatcher(Builder builder) {
        mWatched = builder.mWatched;
        mPreExistingAsCreated = builder.mPreExistingAsCreated;
        mListener = builder.mListener;
        mFilter = builder.mFilter;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void start()
    {
        mWatcherTask = EXECUTOR.submit(this);
    }

    @Override
    public void stop() {
        mWatcherTask.cancel(true);
        mWatcherTask = null;
    }

    @Override
    public void run() {
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException ioe) {
            LOGGER.warn("Exception while creating watch service. Refresh Manually", ioe);
        } finally {
            if (watchService != null) {
                Map<WatchKey, Path> watchKeyToDirectory = new HashMap<>();

                for (Path dir : mWatched) {
                    try {
                        if (mPreExistingAsCreated) {
                            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                                for (Path path : stream) {
                                    if (mFilter.accept(path)) {
                                        mListener.onEvent(Event.ENTRY_CREATE, dir.resolve(path));
                                    }
                                }
                            }
                        }

                        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                        watchKeyToDirectory.put(key, dir);
                    } catch (IOException ioe) {
                        LOGGER.error("Not watching '{}'.", dir, ioe);
                    }
                }
                while (true) {
                    if (Thread.interrupted()) {
                        LOGGER.info("Directory watcher thread interrupted.");
                        break;
                    }

                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        continue;
                    }

                    Path dir = watchKeyToDirectory.get(key);
                    if (dir == null) {
                        LOGGER.warn("Watch key not recognized.");
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind().equals(OVERFLOW)) {
                            break;
                        }

                        WatchEvent<Path> pathEvent = cast(event);
                        WatchEvent.Kind<Path> kind = pathEvent.kind();

                        Path path = dir.resolve(pathEvent.context());
                        try {
                            if (mFilter.accept(path) && EVENT_MAP.containsKey(kind)) {
                                mListener.onEvent(EVENT_MAP.get(kind), path);
                            }
                        } catch (IOException ioe2) {
                            LOGGER.error("Not filtered '{}'.", dir, ioe2);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        watchKeyToDirectory.remove(key);
                        LOGGER.warn("'{}' is inaccessible. Stopping watch.", dir);
                        if (watchKeyToDirectory.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public interface Listener {
        void onEvent(Event event, Path path);
    }

    public static class Builder {

        private static final Filter<Path> NO_FILTER = path -> true;

        private final Set<Path> mWatched = new HashSet<>();
        private boolean mPreExistingAsCreated = false;
        private Filter<Path> mFilter = NO_FILTER;
        private Listener mListener;

        public Builder addDirectories(String dirPath) {
            return addDirectories(Paths.get(dirPath));
        }

        public Builder addDirectories(Path dirPath) {
            mWatched.add(dirPath);
            return this;
        }

        public Builder addDirectories(Path... dirPaths) {
            Collections.addAll(mWatched, dirPaths);
            return this;
        }

        public Builder addDirectories(Iterable<? extends Path> dirPaths) {
            for (Path dirPath : dirPaths) {
                mWatched.add(dirPath);
            }
            return this;
        }

        public Builder setPreExistingAsCreated(boolean value) {
            mPreExistingAsCreated = value;
            return this;
        }

        public Builder setFilter(Filter<Path> filter) {
            mFilter = filter;
            return this;
        }

        public DirectoryWatcher build(Listener listener) {
            mListener = listener;
            return new DirectoryWatcher(this);
        }
    }
}