package com.itsvks.pictures.database

import android.content.Context
import androidx.security.crypto.MasterKey
import com.blankj.utilcode.util.FileUtils
import com.itsvks.pictures.PicturesApp
import com.itsvks.pictures.extensions.encrypt
import com.itsvks.pictures.models.Vault
import com.itsvks.pictures.type.UnitFun
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class KeychainHolder @Inject constructor(
  @ApplicationContext
  private val context: Context
) {
  companion object {
    const val VAULT_INFO_FILE_NAME = "info.vault"
    const val VAULT_INFO_FILE = "/$VAULT_INFO_FILE_NAME"
  }

  val masterKey by lazy {
    MasterKey.Builder(context).apply {
      setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    }.build()
  }

  val filesDir: File = context.filesDir

  fun writeVaultInfo(
    vault: Vault,
    onSuccess: UnitFun = {},
    onFailed: (reason: String) -> Unit = {}
  ) {
    try {
      FileUtils.createOrExistsDir(vaultFolder(vault))

      vaultInfoFile(vault).apply {
        if (exists()) delete()
        encrypt(
          context = context,
          key = masterKey,
          data = vault
        )
        onSuccess()
      }
    } catch (err: Exception) {
      err.printStackTrace()
      onFailed(err.message.toString())
    }
  }

  fun deleteVault(
    vault: Vault,
    onSuccess: UnitFun,
    onFailed: (reason: String) -> Unit
  ) {
    try {
      with(vaultFolder(vault)) {
        if (exists()) deleteRecursively()
      }
      onSuccess()
    } catch (err: Exception) {
      err.printStackTrace()
      onFailed(err.message.toString())
    }
  }

  fun checkVaultFolder(vault: Vault) {
    FileUtils.createOrExistsDir(vaultFolder(vault))
    writeVaultInfo(vault)
  }

  fun vaultFolder(vault: Vault) = File(filesDir, vault.uuid.toString())
  fun vaultInfoFile(vault: Vault) = File(vaultFolder(vault), VAULT_INFO_FILE_NAME)

  fun Vault.mediaFile(mediaId: Long) = File(vaultFolder(this), "$mediaId.enc")
}