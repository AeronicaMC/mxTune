package aeronicamc.mods.mxtune.caps.stages;

import net.minecraftforge.eventbus.api.Event;

public enum StageToolState
{
    Corner1 {
        @Override
        public StageToolState nextState() {
            return Corner2;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Corner2 {
        @Override
        public StageToolState nextState() {
            return AudienceSpawn;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    AudienceSpawn {
        @Override
        public StageToolState nextState() {
            return StageSpawn;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    StageSpawn {
        @Override
        public StageToolState nextState() {
            return Name;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Name {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Corner1Edit {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Corner2Edit {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    AudienceSpawnEdit {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    StageSpawnEdit {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    NameEdit {
        @Override
        public StageToolState nextState() {
            return Apply;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Apply {
        @Override
        public StageToolState nextState() {
            return Done;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    },
    Done {
        @Override
        public StageToolState nextState() {
             return Done;
        }

        @Override
        public Event processRequest() {
            return new Event();
        }
    };

    public abstract StageToolState nextState();

    public abstract Event processRequest();
}
