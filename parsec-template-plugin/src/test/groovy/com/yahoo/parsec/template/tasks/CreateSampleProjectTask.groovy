package com.yahoo.parsec.template.tasks

import org.gradle.api.tasks.TaskAction
/**
 * @author waynewu
 */

class CreateSampleProjectTask extends AbstractProjectTask {

    public CreateSampleProjectTask(){
        super('testing', 'for testing')
    }

    @TaskAction
    void create(){

        //Dummy class
        //Do nothing

    }

}
