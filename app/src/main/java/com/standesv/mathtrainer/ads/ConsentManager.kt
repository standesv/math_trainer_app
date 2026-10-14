package com.standesv.mathtrainer.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Recueil du consentement publicitaire (RGPD) via le User Messaging Platform
 * de Google, puis initialisation du SDK AdMob.
 *
 * Depuis janvier 2024, Google exige une plateforme de consentement certifiee
 * pour diffuser des annonces aux utilisateurs de l'EEE et du Royaume-Uni.
 * Sans elle, les annonces personnalisees cessent simplement d'etre servies.
 *
 * L'application s'adressant a des enfants, deux garde-fous supplementaires
 * sont appliques : voir [CHILD_DIRECTED].
 */
class ConsentManager(private val activity: Activity) {

    companion object {
        private const val TAG = "ConsentManager"

        /**
         * Regime "application destinee aux enfants".
         *
         * A true :
         *  - les annonces sont marquees child-directed (COPPA) ;
         *  - le contenu est limite a la categorie G (tout public) ;
         *  - l'utilisateur est declare sous l'age du consentement, ce qui
         *    desactive les annonces personnalisees.
         *
         * Consequence : les revenus publicitaires sont nettement plus faibles
         * qu'avec des annonces personnalisees. C'est le reglage prudent pour
         * une application de revision destinee a des enfants ; le passer a
         * false releve d'une decision qui t'appartient.
         */
        const val CHILD_DIRECTED = true
    }

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    /** Empeche une double initialisation du SDK AdMob (voir gather). */
    private val adsInitialized = AtomicBoolean(false)

    /** Vrai des que le consentement permet de demander des annonces. */
    val canRequestAds: Boolean get() = consentInformation.canRequestAds()

    /**
     * Certains messages imposent de laisser l'utilisateur revenir sur son
     * choix a tout moment. Dans ce cas un point d'entree visible est
     * obligatoire : c'est le bouton "Confidentialite" des reglages.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * A appeler a chaque lancement : le consentement peut avoir expire ou
     * la reglementation applicable avoir change depuis la derniere session.
     *
     * [onStateChanged] est invoque a chaque fois que l'etat evolue, afin que
     * l'interface puisse afficher la banniere des que c'est permis.
     */
    fun gather(onStateChanged: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(CHILD_DIRECTED)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Le consentement d'une session precedente peut deja suffire :
                // on tente l'initialisation sans attendre le formulaire.
                initializeAdsIfAllowed()
                onStateChanged()

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Formulaire de consentement : ${formError.message}")
                    }
                    initializeAdsIfAllowed()
                    onStateChanged()
                }
            },
            { requestError ->
                // En cas d'echec reseau, le SDK retombe sur le consentement
                // de la session precedente : on tente quand meme.
                Log.w(TAG, "Mise a jour du consentement : ${requestError.message}")
                initializeAdsIfAllowed()
                onStateChanged()
            }
        )
    }

    /** Affiche le formulaire permettant de revenir sur son choix. */
    fun showPrivacyOptions(onDismissed: (FormError?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(
            activity,
            ConsentForm.OnConsentFormDismissedListener { onDismissed(it) }
        )
    }

    /**
     * Initialise AdMob une seule fois, et uniquement si le consentement le
     * permet. L'appel est fait hors du thread principal : une latence reseau
     * ne doit jamais retarder l'affichage de l'application.
     */
    private fun initializeAdsIfAllowed() {
        if (!canRequestAds) return
        if (!adsInitialized.compareAndSet(false, true)) return

        Thread {
            runCatching {
                if (CHILD_DIRECTED) {
                    MobileAds.setRequestConfiguration(
                        RequestConfiguration.Builder()
                            .setTagForChildDirectedTreatment(
                                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                            )
                            .setMaxAdContentRating(
                                RequestConfiguration.MAX_AD_CONTENT_RATING_G
                            )
                            .build()
                    )
                }
                MobileAds.initialize(activity)
            }.onFailure { Log.w(TAG, "Initialisation AdMob : ${it.message}") }
        }.start()
    }
}
