package main_server;

public enum SchedulingAlgorithm {
    FCFS {
        @Override
        public String toString() {
            return "First Come First Serve";
        }
    }, SJF {
        @Override
        public String toString() {
            return "Shortest Job First";
        }
    }, RR {
        @Override
        public String toString() {
            return "Round Robin";
        }
    },
}
