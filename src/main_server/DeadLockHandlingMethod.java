package main_server;

public enum DeadLockHandlingMethod {
    NONE {
        @Override
        public String toString() {
            return "No Deadlock Handling";
        }
    },
    PREVENTION {
        @Override
        public String toString() {
            return "Prevention";
        }
    }
}
