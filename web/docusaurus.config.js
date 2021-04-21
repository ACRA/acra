/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'ACRA',
  tagline: 'Know your bugs.',
  url: 'https://www.acra.ch',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'ACRA', // Usually your GitHub org/user name.
  projectName: 'acra', // Usually your repo name.
  themeConfig: {
    prism: {
      additionalLanguages: ['kotlin', 'groovy', 'java'],
    },
    navbar: {
      title: 'ACRA',
      logo: {
        alt: 'ACRA Logo',
        src: 'img/logo.png',
      },
      items: [
        {
          type: 'doc',
          docId: 'Setup',
          position: 'left',
          label: 'Wiki',
        },
        {to: 'pathname:///javadoc/latest', label: 'Documentation', position: 'left'},
        {
          href: 'https://github.com/facebook/docusaurus',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Wiki',
              to: '/docs/Setup',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/acra',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/facebook/docusaurus',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} ACRA organization. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl: 'https://github.com/acra/acra.github.com/edit/master/website/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
