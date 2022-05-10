package com.mathhelper.mathhelperserver.datatables_history

import com.mathhelper.mathhelperserver.constants.Constants
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotEmpty

@Embeddable
class HistoryId: Serializable {
    constructor() {}
    constructor(code: String, version: Int) {
        this.code = code
        this.version = version
    }

    @NotEmpty(message = "Provide unique name")
    @Column(length = Constants.STRING_LENGTH_LONG)
    var code: String = ""

    @NotEmpty(message = "Provide a version")
    var version: Int = 0
}