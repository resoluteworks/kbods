package org.kbods.read;

import kotlin.Unit;

public class ExamplesJava {

    public void downloadLatest() {
        BodsDownload.Companion.latest().readStatements(
                statement -> {
                    System.out.println(statement.getJsonString());
                    return Unit.INSTANCE;
                }
        );
    }
}
