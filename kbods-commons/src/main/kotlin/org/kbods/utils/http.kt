package org.kbods.utils

import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.OutputStream
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val HTTP_OK = 200
private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
        return arrayOf()
    }
})

private fun httpClientBuilder(
    followRedirects: Boolean,
    ignoreSslErrors: Boolean
): OkHttpClient.Builder {
    val builder = OkHttpClient().newBuilder()
        .connectTimeout(Duration.of(120, ChronoUnit.SECONDS))
        .readTimeout(Duration.of(120, ChronoUnit.SECONDS))
        .followRedirects(followRedirects)

    if (ignoreSslErrors) {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
    }
    return builder
}

fun httpClient(followRedirects: Boolean = true, ignoreSslErrors: Boolean = false): OkHttpClient {
    val builder = httpClientBuilder(followRedirects, ignoreSslErrors)
    return builder.build()
}

fun OkHttpClient.get(url: String, headers: Map<String, String> = emptyMap()): Response {
    val request = Request.Builder()
        .url(url)
        .get()
        .headers(headers.toHeaders())
        .build()
    return newCall(request).execute()
}

fun Response.checkOk(): Response {
    return checkStatus(HTTP_OK)
}

fun Response.checkStatus(vararg successCodes: Int): Response {
    if (successCodes.isNotEmpty() && !successCodes.contains(code)) {
        throw HttpResponseException(this)
    }
    return this
}

fun Response.text(): String {
    val string = this.body!!.string()
    this.body!!.close()
    return string
}

fun Response.writeTo(outputStream: OutputStream) {
    this.body!!.byteStream().copyTo(outputStream)
    this.body!!.close()
}

fun Response.writeTo(file: File): File {
    file.outputStream().use { output ->
        writeTo(output)
    }
    return file
}

class HttpResponseException(url: String, code: Int, responseBody: String) :
    RuntimeException("Error on HTTP request ${url}. Status code is $code and response body is $responseBody") {

    constructor(response: Response) : this(response.request.url.toString(), response.code, response.text())
}

