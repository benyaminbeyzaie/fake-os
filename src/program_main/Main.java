package program_main;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new InputHandler().readInputsAndStartProgram();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
