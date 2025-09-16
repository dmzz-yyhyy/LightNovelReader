package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.importer

sealed interface Wenku8CloudImportError {
    data object NotLoggedIn: Wenku8CloudImportError
    data object Network: Wenku8CloudImportError
    data object Parse: Wenku8CloudImportError
    data class Unknown(val throwable: Throwable): Wenku8CloudImportError
}
