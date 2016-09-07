package test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import classes.Utils;

public class TestSortClusterWords {

	public static void main(String[] args) throws IOException {
		String cluster = "psilocin, LSD, thiopental, antibacterial, crack, phenytoin, peyote, neuroleptic, botox, lidocaine, pethidine, chlorpromazine, prescription, dextromethorphan, mg, fentanyl, pseudoephedrine, digoxin, gentamicin, strychnine, citalopram, anaesthesia, INH, Valium, ?-lactam, phenethylamine, suppository, anticoagulant, infusion, anticholinergic, painkiller, NSAID, Ibuprofen, oxytocin, mg, contraceptive, NRIs, potency, anxiolytic, Botox, ayahuasca, Nicotine, relaxant, supplement, narcotic, intravenously, thinner, statin, NSAIDs, cyclophosphamide, enhancer, Paracetamol, morphine, nsaid, midazolam, temazepam, IUD, chloroquine, theophylline, depressant, carbapenem, Prozac, injection, anti-depressant, dose, carbamazepine, fluoroquinolone, paclitaxel, sedative, ketamine, prednisone, monotherapy, valproate, naproxen, MDMA, streptomycin, blocker, antiandrogen, antiretroviral, corticosteroid, Opium, purgative, nandrolone, Tylenol, placebo, Tamiflu, tuberculin, generic, Medication, rapamycin, Paxil, ketoconazole, Heroin, prophylaxis, methadone, anesthetic, dissociative, Vioxx, vasodilator, Mescaline, reuptake, methamphetamine, analgesic, artemisinin, Niacin, Ritalin, inhalant, diet, antitumor, isotretinoin, smoking, heroin, aspirin, stimulant, Drug, oseltamivir, naltrexone, amiodarone, ciprofloxacin, hypnotic, diuretic, chloramphenicol, regimen, astringent, steroid, zolpidem, paroxetine, BZP, THC, aphrodisiac, bupropion, anesthesia, baclofen, epinephrine, rinse, penicillin, rifampicin, enema, endorphin, amitriptyline, meth, orally, amoxicillin, bisphosphonate, antidepressant, progestogen, antagonists, SSRIs, tranquilizer, mephedrone, caffeine, black-market, Alcohol, Nectar, curare, agonist, venlafaxine, Cannabis, antifungal, modafinil, Peyote, penicillin, immunosuppressive, quinine, azathioprine, HRT, amphetamine, antihistamine, ephedrine, MDA, metronidazole, Gleevec, ribavirin, paracetamol, glucocorticoid, aminoglycoside, ganja, ibogaine, DES, syringe, mifepristone, cyclosporine, dabigatran, abortifacient, ecstasy, anti-inflammatory, acetaminophen, methylphenidate, Marijuana, vaccine, antiviral, Vicodin, tramadol, divinorum, SNRIs, cortisone, hydrate, antimicrobial, clarithromycin, clarithromycin, suppressant, Adderall, remedy, doxorubicin, digitali, flunitrazepam, OTC, ivermectin, tetracycline, tamoxifen, antitoxin, Cocaine, anaesthetic, colchicine, divinorum, fluoxetine, injectable, pill, cannabinoid, propranolol, doxycycline, radiotherapy, paraphernalia, Xanax, laudanum, Amphetamine, metformin, drug, antimalarial, GHB, beta-blocker, vancomycin, alprazolam, lovastatin, quinolone, derivatives, DOM, thrombolysis, psychoactive, clozapine, Ketamine, haloperidol, adjuvant, Inhibitors, methotrexate, opiate, simvastatin, atropine, IVIG, warfarin, lorazepam, Viagra, cimetidine, hydrocodone, moclobemide, antihypertensive, ibuprofen, antiemetic, tablet, HGH, furosemide, zopiclone, antibiotic, imatinib, mg/day, propofol, azithromycin, multivitamin, lsd, androgen, diazepam, naloxone, e.g., HAART, microbicide, clonazepam, scopolamine, immunosuppressant, compounding, TCAs, dexamethasone, Opioid, cannabis, opioid, DMT, buprenorphine, albendazole, PMA, IUDs, analgesia, benzodiazepine, agent, psilocybin, bronchodilator, acupuncture, mescaline, tacrolimus, ergot, TCA, contraception, Penicillin, erythromycin, levodopa, progestin, alcohol, anthelmintic, cannabis, Methamphetamine, ciclosporin, inhibitor, porn, olanzapine, nitrazepam, insulin, dosage, laxative, sulfonylurea, thalidomide, inhaler, hydrocortisone, stanozolol, Topical, intramuscular, Methadone, decongestant, oxycodone, gabapentin, psychedelic, Ecstasy, ampicillin, psychostimulant, medication, salvia, antivenom, SSRI, clindamycin, ECT, PCP, cephalosporin, phenobarbital, macrolide, chemotherapy, finasteride, hallucinogen, bolus, codeine, entheogen, Codeine, tryptamine, clonidine, Narcotics, cocaine, vasoconstrictor, diphenhydramine, ssri, chemo, deliriant, CPAP, Caffeine, anticonvulsant, Morphine, marijuana, prodrug, sulfonamide, sertraline, antidote, iud, MAOIs, AZT, cisplatin, barbiturate, antacid, antipsychotic, Aspirin, reliever, heparin, antiseptic, addict";
		Utils u= new Utils();
		Map<String,Integer> freqMap = u.buildFreqMap("/news100M_stanford_cc_word_count");
		Set<String> set = new HashSet<String>();
		for (int i = 0; i<cluster.split(", ").length;i++){
			set.add(cluster.split(", ")[i]);
		}
		cluster = u.sortClusterByFrequency(set, freqMap);
		System.out.println(cluster);
	}

}
